package br.ufrn.imd.imdtravel.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ExchangeClientTest {

	private static final String BASE_URL = "http://exchange:8080";
	private static final OffsetDateTime AS_OF = OffsetDateTime.parse("2026-11-18T15:30:00-03:00");

	@Test
	void getsConversionAndMapsResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		ExchangeClient client = new ExchangeClient(builder.build(), properties());

		server.expect(requestTo(BASE_URL + "/convert"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"rate\":5.25,\"asOf\":\"2026-11-18T15:30:00-03:00\"}",
						MediaType.APPLICATION_JSON));

		ExchangeClient.ConversionResponse response = client.getConversion();

		assertEquals(new BigDecimal("5.25"), response.rate());
		assertEquals(AS_OF.toInstant(), response.asOf().toInstant());
		server.verify();
	}

	private ExternalServicesProperties properties() {
		ExternalServicesProperties properties = new ExternalServicesProperties();
		properties.getExchange().setBaseUrl(URI.create(BASE_URL));
		return properties;
	}
}
