package br.ufrn.imd.imdtravel.sales;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import br.ufrn.imd.imdtravel.client.AirlinesHubClient;
import br.ufrn.imd.imdtravel.client.ExchangeClient;
import br.ufrn.imd.imdtravel.client.FidelityClient;
import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class BuyTicketService {

	private final AirlinesHubClient airlinesHubClient;
	private final ExchangeClient exchangeClient;
	private final FidelityClient fidelityClient;
	private final SalesRepository salesRepository;

	public BuyTicketService(
			AirlinesHubClient airlinesHubClient,
			ExchangeClient exchangeClient,
			FidelityClient fidelityClient,
			SalesRepository salesRepository) {
		this.airlinesHubClient = airlinesHubClient;
		this.exchangeClient = exchangeClient;
		this.fidelityClient = fidelityClient;
		this.salesRepository = salesRepository;
	}

	public BuyTicketResponse buy(BuyTicketRequest request) throws IOException {
		AirlinesHubClient.FlightResponse flight;
		try {
			flight = airlinesHubClient.getFlight(request.flight(), request.day());
		}
		catch (HttpClientErrorException.NotFound exception) {
			throw new FlightNotFoundException(exception);
		}

		ExchangeClient.ConversionResponse conversion = exchangeClient.getConversion();

		AirlinesHubClient.SellResponse sale;
		try {
			sale = airlinesHubClient.sellFlight(request.flight(), request.day());
		}
		catch (HttpClientErrorException.Conflict exception) {
			throw new SoldOutException(exception);
		}

		int bonus = flight.priceUSD().setScale(0, RoundingMode.FLOOR).intValueExact();
		fidelityClient.creditBonus(request.user(), bonus);

		BigDecimal valueBRL = flight.priceUSD()
				.multiply(conversion.rate())
				.setScale(2, RoundingMode.HALF_UP);
		String correlationId = CorrelationIdContext.getCorrelationId();
		salesRepository.add(new SaleRecord(
				correlationId,
				sale.transactionId(),
				request.flight(),
				request.day(),
				request.user(),
				flight.priceUSD(),
				valueBRL,
				Instant.now().toString()));

		return new BuyTicketResponse(
				sale.transactionId(),
				request.flight(),
				request.day(),
				request.user(),
				flight.priceUSD(),
				valueBRL,
				conversion.rate(),
				conversion.asOf(),
				bonus);
	}
}
