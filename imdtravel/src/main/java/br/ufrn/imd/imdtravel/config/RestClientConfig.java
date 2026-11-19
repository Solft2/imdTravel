package br.ufrn.imd.imdtravel.config;

import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

	private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

	@Bean
	public RestClient restClient() {
		return buildRestClient(RestClient.builder());
	}

	RestClient buildRestClient(RestClient.Builder builder) {
		ClientHttpRequestInterceptor correlationIdInterceptor = (request, body, execution) -> {
			String correlationId = CorrelationIdContext.getCorrelationId();
			if (StringUtils.hasText(correlationId)) {
				request.getHeaders().set(CORRELATION_ID_HEADER, correlationId);
			}
			return execution.execute(request, body);
		};

		return builder
				.requestInterceptor(correlationIdInterceptor)
				.build();
	}
}
