package br.ufrn.imd.imdtravel.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import br.ufrn.imd.imdtravel.service.FlightNotFoundException;
import br.ufrn.imd.imdtravel.service.PurchaseService;
import br.ufrn.imd.imdtravel.service.SoldOutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;

class PurchaseControllerTest {

	private final PurchaseService purchaseService = org.mockito.Mockito.mock(PurchaseService.class);

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(new PurchaseController(purchaseService))
				.setControllerAdvice(new ApiExceptionHandler(new StructuredEventLogger()))
				.build();
	}

	@Test
	void returnsCreatedPurchaseResponse() throws Exception {
		when(purchaseService.buyTicket("IM100", "2026-12-01", "ana"))
				.thenReturn(new PurchaseService.PurchaseResponse(
						"txn-1",
						"IM100",
						"2026-12-01",
						"ana",
						new BigDecimal("350.50"),
						new BigDecimal("1927.75"),
						new BigDecimal("5.50"),
						OffsetDateTime.parse("2026-11-18T15:30:00Z"),
						350));

		mockMvc.perform(post("/buyTicket")
						.contentType("application/json")
						.content("{\"flight\":\"IM100\",\"day\":\"2026-12-01\",\"user\":\"ana\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.transactionId").value("txn-1"))
				.andExpect(jsonPath("$.valueBRL").value(1927.75))
				.andExpect(jsonPath("$.bonus").value(350));

		verify(purchaseService).buyTicket("IM100", "2026-12-01", "ana");
	}

	@Test
	void rejectsMissingOrBlankFields() throws Exception {
		mockMvc.perform(post("/buyTicket")
						.contentType("application/json")
						.content("{\"flight\":\"IM100\",\"day\":\"2026-12-01\",\"user\":\" \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("invalid-request"));
	}

	@Test
	void mapsMissingFlightToNotFoundAndSoldOutToConflict() throws Exception {
		when(purchaseService.buyTicket("MISSING", "2026-12-01", "ana"))
				.thenThrow(new FlightNotFoundException(new RuntimeException("not found")));
		when(purchaseService.buyTicket("IM100", "2026-12-01", "ana"))
				.thenThrow(new SoldOutException(new RuntimeException("sold out")));

		mockMvc.perform(post("/buyTicket")
						.contentType("application/json")
						.content("{\"flight\":\"MISSING\",\"day\":\"2026-12-01\",\"user\":\"ana\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("not-found"));
		mockMvc.perform(post("/buyTicket")
						.contentType("application/json")
						.content("{\"flight\":\"IM100\",\"day\":\"2026-12-01\",\"user\":\"ana\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("sold-out"));
	}

	@Test
	void mapsDependencyFailureToInternalServerError() throws Exception {
		when(purchaseService.buyTicket("IM100", "2026-12-01", "ana"))
				.thenThrow(new ResourceAccessException("service unavailable"));

		mockMvc.perform(post("/buyTicket")
						.contentType("application/json")
						.content("{\"flight\":\"IM100\",\"day\":\"2026-12-01\",\"user\":\"ana\"}"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.error").value("dependency-failure"));
	}
}
