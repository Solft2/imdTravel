package br.ufrn.imd.imdtravel.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class SalesRepositoryTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void persistsSalesAcrossRepositoryInstances() {
		SalesRepository firstRepository = new SalesRepository(JsonMapper.builder().findAndAddModules().build(),
				temporaryDirectory.toString());
		SaleRecord sale = new SaleRecord(
				"cid-1",
				"txn-1",
				"IM100",
				"2026-12-01",
				"ana",
				new BigDecimal("350.50"),
				new BigDecimal("1927.75"),
				OffsetDateTime.parse("2026-11-21T17:00:00Z"));

		firstRepository.add(sale);

		SalesRepository restartedRepository = new SalesRepository(JsonMapper.builder().findAndAddModules().build(),
				temporaryDirectory.toString());

		assertEquals(java.util.List.of(sale), restartedRepository.findAll());
	}
}
