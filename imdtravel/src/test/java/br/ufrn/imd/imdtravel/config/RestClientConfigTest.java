package br.ufrn.imd.imdtravel.config;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.headerDoesNotExist;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestClientConfigTest {

	private static final String REQUEST_URI = "https://example.test/itinerary";
	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

	@AfterEach
	void clearCorrelationId() {
		CorrelationIdContext.clear();
	}

	@Test
	void propagatesCorrelationIdFromContext() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient client = new RestClientConfig().buildRestClient(builder);
		String correlationId = "request-123";
		CorrelationIdContext.setCorrelationId(correlationId);

		server.expect(requestTo(REQUEST_URI))
				.andExpect(header(CORRELATION_ID_HEADER, correlationId))
				.andRespond(withSuccess());

		client.get().uri(REQUEST_URI).retrieve().toBodilessEntity();

		server.verify();
	}

	@Test
	void doesNotAddCorrelationIdWhenContextIsEmpty() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		RestClient client = new RestClientConfig().buildRestClient(builder);

		server.expect(requestTo(REQUEST_URI))
				.andExpect(headerDoesNotExist(CORRELATION_ID_HEADER))
				.andRespond(withSuccess());

		client.get().uri(REQUEST_URI).retrieve().toBodilessEntity();

		server.verify();
	}
}
