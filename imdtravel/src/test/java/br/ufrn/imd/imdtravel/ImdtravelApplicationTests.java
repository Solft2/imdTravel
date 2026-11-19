package br.ufrn.imd.imdtravel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ImdtravelApplicationTests {

	@Autowired
	private ExternalServicesProperties externalServicesProperties;

	@Test
	void contextLoads() {
		assertEquals(URI.create("http://airlineshub:8080"),
				externalServicesProperties.getAirlineshub().getBaseUrl());
		assertEquals(URI.create("http://exchange:8080"),
				externalServicesProperties.getExchange().getBaseUrl());
		assertEquals(URI.create("http://fidelity:8080"),
				externalServicesProperties.getFidelity().getBaseUrl());
	}

}
