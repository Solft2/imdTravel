package br.ufrn.imd.imdtravel.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.net.URI;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

class AirlinesHubClientTest {

	private static final String BASE_URL = "http://airlineshub:8080";
	private static final String FLIGHT = "IM123";
	private static final String DAY = "2026-11-18";

	@Test
	void getsFlightAndMapsResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AirlinesHubClient client = new AirlinesHubClient(builder.build(), properties());

		server.expect(requestTo(BASE_URL + "/flight?flight=" + FLIGHT + "&day=" + DAY))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withSuccess(
						"{\"flight\":\"IM123\",\"day\":\"2026-11-18\",\"priceUSD\":325.50,\"seatsAvailable\":7}",
						MediaType.APPLICATION_JSON));

		AirlinesHubClient.FlightResponse response = client.getFlight(FLIGHT, DAY);

		assertEquals(FLIGHT, response.flight());
		assertEquals(DAY, response.day());
		assertEquals(new BigDecimal("325.50"), response.priceUSD());
		assertEquals(7, response.seatsAvailable());
		server.verify();
	}

	@Test
	void postsSellRequestAndMapsResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AirlinesHubClient client = new AirlinesHubClient(builder.build(), properties());

		server.expect(requestTo(BASE_URL + "/sell"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().json("{\"flight\":\"IM123\",\"day\":\"2026-11-18\"}"))
				.andRespond(withStatus(HttpStatus.CREATED)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"transactionId\":\"txn-456\",\"seatsRemaining\":6}"));

		AirlinesHubClient.SellResponse response = client.sellFlight(FLIGHT, DAY);

		assertEquals("txn-456", response.transactionId());
		assertEquals(6, response.seatsRemaining());
		server.verify();
	}

	@Test
	void propagatesConflictWhenNoSeatsAreAvailable() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		AirlinesHubClient client = new AirlinesHubClient(builder.build(), properties());

		server.expect(requestTo(BASE_URL + "/sell"))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.CONFLICT));

		assertThrows(HttpClientErrorException.Conflict.class,
				() -> client.sellFlight(FLIGHT, DAY));
		server.verify();
	}

	private ExternalServicesProperties properties() {
		ExternalServicesProperties properties = new ExternalServicesProperties();
		properties.getAirlineshub().setBaseUrl(URI.create(BASE_URL));
		return properties;
	}
}
