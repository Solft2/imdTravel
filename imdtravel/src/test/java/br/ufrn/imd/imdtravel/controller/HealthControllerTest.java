package br.ufrn.imd.imdtravel.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

	@Test
	void reportsHealthyStatusAndRuntimeUptime() {
		HealthController.HealthResponse response = new HealthController().health();

		assertEquals("ok", response.status());
		assertTrue(response.uptimeS() >= 0);
	}
}
