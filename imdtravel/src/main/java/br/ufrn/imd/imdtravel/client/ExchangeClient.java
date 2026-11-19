package br.ufrn.imd.imdtravel.client;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ExchangeClient {

	private final RestClient restClient;

	private final URI baseUrl;

	public ExchangeClient(RestClient restClient, ExternalServicesProperties properties) {
		this.restClient = restClient;
		this.baseUrl = properties.getExchange().getBaseUrl();
	}

	public ConversionResponse getConversion() {
		URI uri = UriComponentsBuilder.fromUri(baseUrl)
				.pathSegment("convert")
				.build()
				.toUri();

		return restClient.get()
				.uri(uri)
				.retrieve()
				.body(ConversionResponse.class);
	}

	public record ConversionResponse(BigDecimal rate, OffsetDateTime asOf) {
	}
}
