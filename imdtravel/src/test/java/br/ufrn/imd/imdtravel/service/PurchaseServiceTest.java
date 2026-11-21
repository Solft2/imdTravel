package br.ufrn.imd.imdtravel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.ufrn.imd.imdtravel.client.AirlinesHubClient;
import br.ufrn.imd.imdtravel.client.ExchangeClient;
import br.ufrn.imd.imdtravel.client.FidelityClient;
import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import br.ufrn.imd.imdtravel.sales.SaleRecord;
import br.ufrn.imd.imdtravel.sales.SalesRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

class PurchaseServiceTest {

	private final AirlinesHubClient airlinesHubClient = org.mockito.Mockito.mock(AirlinesHubClient.class);

	private final ExchangeClient exchangeClient = org.mockito.Mockito.mock(ExchangeClient.class);

	private final FidelityClient fidelityClient = org.mockito.Mockito.mock(FidelityClient.class);

	private final SalesRepository salesRepository = org.mockito.Mockito.mock(SalesRepository.class);

	private final StructuredEventLogger eventLogger = org.mockito.Mockito.mock(StructuredEventLogger.class);

	private final PurchaseService purchaseService = new PurchaseService(
			airlinesHubClient,
			exchangeClient,
			fidelityClient,
			salesRepository,
			eventLogger);

	@AfterEach
	void clearCorrelationId() {
		CorrelationIdContext.clear();
	}

	@Test
	void callsDependenciesSequentiallyAndPersistsCompletedPurchase() {
		CorrelationIdContext.setCorrelationId("request-123");
		OffsetDateTime asOf = OffsetDateTime.parse("2026-11-18T15:30:00-03:00");
		when(airlinesHubClient.getFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.FlightResponse("IM100", "2026-12-01",
						new BigDecimal("350.50"), 20));
		when(exchangeClient.getConversion())
				.thenReturn(new ExchangeClient.ConversionResponse(new BigDecimal("5.50"), asOf));
		when(airlinesHubClient.sellFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.SellResponse("txn-1", 19));
		when(fidelityClient.awardBonus("ana", 350))
				.thenReturn(new FidelityClient.BonusResponse("ana", 350));

		PurchaseService.PurchaseResponse response = purchaseService.buyTicket("IM100", "2026-12-01", "ana");

		assertEquals("txn-1", response.transactionId());
		assertEquals(new BigDecimal("350.50"), response.valueUSD());
		assertEquals(new BigDecimal("1927.75"), response.valueBRL());
		assertEquals(new BigDecimal("5.50"), response.rate());
		assertEquals(350, response.bonus());

		InOrder order = inOrder(airlinesHubClient, exchangeClient, fidelityClient);
		order.verify(airlinesHubClient).getFlight("IM100", "2026-12-01");
		order.verify(exchangeClient).getConversion();
		order.verify(airlinesHubClient).sellFlight("IM100", "2026-12-01");
		order.verify(fidelityClient).awardBonus("ana", 350);

		verify(salesRepository).add(org.mockito.ArgumentMatchers.argThat(
				(SaleRecord sale) -> sale.correlationId().equals("request-123")
						&& sale.transactionId().equals("txn-1")
						&& sale.valueBRL().equals(new BigDecimal("1927.75"))));
	}

	@Test
	void mapsMissingFlightWithoutCallingLaterDependencies() {
		when(airlinesHubClient.getFlight("MISSING", "2026-12-01"))
				.thenThrow(HttpClientErrorException.create(
						HttpStatus.NOT_FOUND, "Not Found", HttpHeaders.EMPTY, new byte[0], null));

		assertThrows(FlightNotFoundException.class,
				() -> purchaseService.buyTicket("MISSING", "2026-12-01", "ana"));

		verify(exchangeClient, never()).getConversion();
		verify(airlinesHubClient, never()).sellFlight("MISSING", "2026-12-01");
		verify(fidelityClient, never()).awardBonus("ana", 0);
	}

	@Test
	void mapsSoldOutAndDoesNotAwardBonus() {
		when(airlinesHubClient.getFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.FlightResponse("IM100", "2026-12-01",
						new BigDecimal("350.50"), 0));
		when(exchangeClient.getConversion())
				.thenReturn(new ExchangeClient.ConversionResponse(
						new BigDecimal("5.50"), OffsetDateTime.parse("2026-11-18T15:30:00Z")));
		when(airlinesHubClient.sellFlight("IM100", "2026-12-01"))
				.thenThrow(HttpClientErrorException.create(
						HttpStatus.CONFLICT, "Conflict", HttpHeaders.EMPTY, new byte[0], null));

		assertThrows(SoldOutException.class,
				() -> purchaseService.buyTicket("IM100", "2026-12-01", "ana"));

		verify(fidelityClient, never()).awardBonus("ana", 350);
		verify(salesRepository, never()).add(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void stopsBeforeSellingWhenExchangeFails() {
		when(airlinesHubClient.getFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.FlightResponse("IM100", "2026-12-01",
						new BigDecimal("350.50"), 20));
		when(exchangeClient.getConversion()).thenThrow(new ResourceAccessException("exchange unavailable"));

		assertThrows(ResourceAccessException.class,
				() -> purchaseService.buyTicket("IM100", "2026-12-01", "ana"));

		verify(airlinesHubClient, never()).sellFlight("IM100", "2026-12-01");
		verify(fidelityClient, never()).awardBonus("ana", 350);
		verify(salesRepository, never()).add(org.mockito.ArgumentMatchers.any());
	}

	@Test
	void doesNotRecordSaleWhenFidelityFailsAfterAirlineSale() {
		when(airlinesHubClient.getFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.FlightResponse("IM100", "2026-12-01",
						new BigDecimal("350.50"), 20));
		when(exchangeClient.getConversion())
				.thenReturn(new ExchangeClient.ConversionResponse(
						new BigDecimal("5.50"), OffsetDateTime.parse("2026-11-18T15:30:00Z")));
		when(airlinesHubClient.sellFlight("IM100", "2026-12-01"))
				.thenReturn(new AirlinesHubClient.SellResponse("txn-1", 19));
		when(fidelityClient.awardBonus("ana", 350))
				.thenThrow(new ResourceAccessException("fidelity unavailable"));

		assertThrows(ResourceAccessException.class,
				() -> purchaseService.buyTicket("IM100", "2026-12-01", "ana"));

		verify(airlinesHubClient).sellFlight("IM100", "2026-12-01");
		verify(salesRepository, never()).add(org.mockito.ArgumentMatchers.any());
	}
}
