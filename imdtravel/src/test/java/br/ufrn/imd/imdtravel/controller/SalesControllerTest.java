package br.ufrn.imd.imdtravel.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import br.ufrn.imd.imdtravel.sales.SaleRecord;
import br.ufrn.imd.imdtravel.sales.SalesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SalesControllerTest {

	private final SalesRepository salesRepository = org.mockito.Mockito.mock(SalesRepository.class);

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new SalesController(salesRepository)).build();
	}

	@Test
	void returnsPersistedSales() throws Exception {
		when(salesRepository.findAll()).thenReturn(List.of(new SaleRecord(
				"cid-1",
				"txn-1",
				"IM100",
				"2026-12-01",
				"ana",
				new BigDecimal("350.50"),
				new BigDecimal("1927.75"),
				OffsetDateTime.parse("2026-11-21T17:00:00Z"))));

		mockMvc.perform(get("/sales"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].correlationId").value("cid-1"))
				.andExpect(jsonPath("$[0].transactionId").value("txn-1"))
				.andExpect(jsonPath("$[0].flight").value("IM100"))
				.andExpect(jsonPath("$[0].user").value("ana"))
				.andExpect(jsonPath("$[0].valueBRL").value(1927.75))
				.andExpect(jsonPath("$[0].ts").exists());
	}
}
