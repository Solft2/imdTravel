package br.ufrn.imd.imdtravel.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.function.Supplier;

import br.ufrn.imd.imdtravel.client.AirlinesHubClient;
import br.ufrn.imd.imdtravel.client.ExchangeClient;
import br.ufrn.imd.imdtravel.client.FidelityClient;
import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import br.ufrn.imd.imdtravel.sales.SaleRecord;
import br.ufrn.imd.imdtravel.sales.SalesRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Service
public class PurchaseService {

	private final AirlinesHubClient airlinesHubClient;

	private final ExchangeClient exchangeClient;

	private final FidelityClient fidelityClient;

	private final SalesRepository salesRepository;

	private final StructuredEventLogger eventLogger;

	public PurchaseService(
			AirlinesHubClient airlinesHubClient,
			ExchangeClient exchangeClient,
			FidelityClient fidelityClient,
			SalesRepository salesRepository,
			StructuredEventLogger eventLogger) {
		this.airlinesHubClient = airlinesHubClient;
		this.exchangeClient = exchangeClient;
		this.fidelityClient = fidelityClient;
		this.salesRepository = salesRepository;
		this.eventLogger = eventLogger;
	}

	public PurchaseResponse buyTicket(String flight, String day, String user) {
		AirlinesHubClient.FlightResponse flightResponse;
		try {
			flightResponse = callExternal(
					"airlineshub.flight",
					() -> airlinesHubClient.getFlight(flight, day));
		}
		catch (HttpClientErrorException.NotFound exception) {
			throw new FlightNotFoundException(exception);
		}
		if (flightResponse == null || flightResponse.priceUSD() == null) {
			throw new IllegalStateException("AirlinesHub returned an incomplete flight response");
		}

		ExchangeClient.ConversionResponse conversion = callExternal(
				"exchange.convert",
				exchangeClient::getConversion);
		if (conversion == null || conversion.rate() == null || conversion.asOf() == null) {
			throw new IllegalStateException("Exchange returned an incomplete conversion response");
		}

		BigDecimal valueUSD = flightResponse.priceUSD();
		if (valueUSD.signum() < 0 || conversion.rate().signum() <= 0) {
			throw new IllegalStateException("External services returned invalid price or exchange rate");
		}
		BigDecimal valueBRL = valueUSD.multiply(conversion.rate()).setScale(2, RoundingMode.HALF_UP);
		int bonus = valueUSD.setScale(0, RoundingMode.FLOOR).intValueExact();

		AirlinesHubClient.SellResponse sale;
		try {
			sale = callExternal(
					"airlineshub.sell",
					() -> airlinesHubClient.sellFlight(flight, day));
		}
		catch (HttpClientErrorException.Conflict exception) {
			throw new SoldOutException(exception);
		}
		if (sale == null || sale.transactionId() == null || sale.transactionId().isBlank()) {
			throw new IllegalStateException("AirlinesHub returned an incomplete sale response");
		}

		FidelityClient.BonusResponse bonusResponse = callExternal(
				"fidelity.bonus",
				() -> fidelityClient.awardBonus(user, bonus));
		if (bonusResponse == null || !user.equals(bonusResponse.user()) || bonusResponse.totalPoints() < bonus) {
			throw new IllegalStateException("Fidelity returned an invalid bonus response");
		}

		OffsetDateTime soldAt = OffsetDateTime.now(ZoneOffset.UTC);
		salesRepository.add(new SaleRecord(
				CorrelationIdContext.getCorrelationId(),
				sale.transactionId(),
				flight,
				day,
				user,
				valueUSD,
				valueBRL,
				soldAt));

		return new PurchaseResponse(
				sale.transactionId(),
				flight,
				day,
				user,
				valueUSD,
				valueBRL,
				conversion.rate(),
				conversion.asOf(),
				bonus);
	}

	private <T> T callExternal(String service, Supplier<T> request) {
		long startedAt = System.nanoTime();
		try {
			T response = request.get();
			eventLogger.log(service + ".completed", elapsedMilliseconds(startedAt));
			return response;
		}
		catch (RestClientException exception) {
			eventLogger.log(service + ".failed", elapsedMilliseconds(startedAt));
			throw exception;
		}
	}

	private long elapsedMilliseconds(long startedAt) {
		return (System.nanoTime() - startedAt) / 1_000_000;
	}

	public record PurchaseResponse(
			String transactionId,
			String flight,
			String day,
			String user,
			BigDecimal valueUSD,
			BigDecimal valueBRL,
			BigDecimal rate,
			OffsetDateTime rateAsOf,
			int bonus) {
	}
}
