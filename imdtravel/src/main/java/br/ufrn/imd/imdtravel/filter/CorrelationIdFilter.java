package br.ufrn.imd.imdtravel.filter;

import java.io.IOException;
import java.util.UUID;

import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

	public static final String HEADER_NAME = "X-Correlation-Id";

	private final StructuredEventLogger eventLogger;

	public CorrelationIdFilter(StructuredEventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String correlationId = request.getHeader(HEADER_NAME);
		if (correlationId == null || correlationId.isBlank()) {
			correlationId = UUID.randomUUID().toString();
		}

		CorrelationIdContext.setCorrelationId(correlationId);
		MDC.put("cid", correlationId);
		long startedAt = System.nanoTime();
		try {
			response.setHeader(HEADER_NAME, correlationId);
			filterChain.doFilter(request, response);
		}
		finally {
			try {
				eventLogger.log("http.request.completed", (System.nanoTime() - startedAt) / 1_000_000);
			}
			finally {
				MDC.remove("cid");
				CorrelationIdContext.clear();
			}
		}
	}
}
