package br.ufrn.imd.imdtravel.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FidelityClientTest {

	private static final String BASE_URL = "http://fidelity:8080";

	@Test
	void postsBonusAndMapsResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		FidelityClient client = new FidelityClient(builder.build(), properties());

		server.expect(requestTo(BASE_URL + "/bonus"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"user\":\"ana\",\"bonus\":350}"))
				.andRespond(withStatus(HttpStatus.CREATED)
						.body("{\"user\":\"ana\",\"totalPoints\":350}")
						.contentType(MediaType.APPLICATION_JSON));

		FidelityClient.BonusResponse response = client.creditBonus("ana", 350);

		assertEquals("ana", response.user());
		assertEquals(350, response.totalPoints());
		server.verify();
	}

	private ExternalServicesProperties properties() {
		ExternalServicesProperties properties = new ExternalServicesProperties();
		properties.getFidelity().setBaseUrl(URI.create(BASE_URL));
		return properties;
	}
}
