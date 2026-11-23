package br.ufrn.imd.imdtravel.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.ufrn.imd.imdtravel.client.AirlinesHubClient;
import br.ufrn.imd.imdtravel.client.ExchangeClient;
import br.ufrn.imd.imdtravel.client.FidelityClient;
import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class BuyTicketServiceTest {

	private final AirlinesHubClient airlinesHubClient = mock(AirlinesHubClient.class);
	private final ExchangeClient exchangeClient = mock(ExchangeClient.class);
	private final FidelityClient fidelityClient = mock(FidelityClient.class);
	private final SalesRepository salesRepository = mock(SalesRepository.class);
	private final BuyTicketService service = new BuyTicketService(
			airlinesHubClient,
			exchangeClient,
			fidelityClient,
			salesRepository);

	@AfterEach
	void clearCorrelationId() {
		CorrelationIdContext.clear();
	}

	@Test
	void completesCallsInOrderAndRecordsTheSale() throws Exception {
		String flightCode = "IMD100";
		String day = "2026-12-01";
		CorrelationIdContext.setCorrelationId("cid-123");
		when(airlinesHubClient.getFlight(flightCode, day))
				.thenReturn(new AirlinesHubClient.FlightResponse(
						flightCode, day, new BigDecimal("350.50"), 20));
		when(exchangeClient.getConversion())
				.thenReturn(new ExchangeClient.ConversionResponse(
						new BigDecimal("5.50"),
						OffsetDateTime.parse("2026-11-18T15:30:00-03:00")));
		when(airlinesHubClient.sellFlight(flightCode, day))
				.thenReturn(new AirlinesHubClient.SellResponse("txn-1", 19));

		BuyTicketResponse response = service.buy(new BuyTicketRequest(flightCode, day, "ana"));

		assertEquals("txn-1", response.transactionId());
		assertEquals(new BigDecimal("350.50"), response.valueUSD());
		assertEquals(new BigDecimal("1927.75"), response.valueBRL());
		assertEquals(350, response.bonus());

		InOrder order = inOrder(airlinesHubClient, exchangeClient, fidelityClient, salesRepository);
		order.verify(airlinesHubClient).getFlight(flightCode, day);
		order.verify(exchangeClient).getConversion();
		order.verify(airlinesHubClient).sellFlight(flightCode, day);
		order.verify(fidelityClient).creditBonus("ana", 350);
		ArgumentCaptor<SaleRecord> saleCaptor = ArgumentCaptor.forClass(SaleRecord.class);
		order.verify(salesRepository).add(saleCaptor.capture());
		assertEquals("cid-123", saleCaptor.getValue().correlationId());
		assertEquals("txn-1", saleCaptor.getValue().transactionId());
		assertEquals("ana", saleCaptor.getValue().user());
	}
}
