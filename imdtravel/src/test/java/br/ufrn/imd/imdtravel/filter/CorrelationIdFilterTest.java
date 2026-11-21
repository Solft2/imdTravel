package br.ufrn.imd.imdtravel.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.UUID;

import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.slf4j.MDC;

class CorrelationIdFilterTest {

	private final CorrelationIdFilter filter = new CorrelationIdFilter(new StructuredEventLogger());

	@Test
	void reusesIncomingIdAndMakesItAvailableDuringRequest() throws Exception {
		String correlationId = "client-request-123";
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(CorrelationIdFilter.HEADER_NAME, correlationId);
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (servletRequest, servletResponse) ->
		{
			assertEquals(correlationId, CorrelationIdContext.getCorrelationId());
			assertEquals(correlationId, MDC.get("cid"));
		});

		assertEquals(correlationId, response.getHeader(CorrelationIdFilter.HEADER_NAME));
		assertNull(CorrelationIdContext.getCorrelationId());
		assertNull(MDC.get("cid"));
	}

	@Test
	void generatesUuidWhenIncomingIdIsMissing() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, (servletRequest, servletResponse) -> {
			String correlationId = CorrelationIdContext.getCorrelationId();
			assertEquals(correlationId, response.getHeader(CorrelationIdFilter.HEADER_NAME));
			assertEquals(correlationId, UUID.fromString(correlationId).toString());
		});

		assertNull(CorrelationIdContext.getCorrelationId());
	}

	@Test
	void clearsContextWhenRequestProcessingFails() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		assertThrows(IOException.class, () -> filter.doFilter(request, response,
				(servletRequest, servletResponse) -> {
					throw new IOException("request processing failed");
				}));

		assertNull(CorrelationIdContext.getCorrelationId());
	}
}
