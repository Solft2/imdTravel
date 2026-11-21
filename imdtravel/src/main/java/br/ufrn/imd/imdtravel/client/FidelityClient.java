package br.ufrn.imd.imdtravel.client;

import java.net.URI;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FidelityClient {

	private final RestClient restClient;

	private final URI baseUrl;

	public FidelityClient(RestClient restClient, ExternalServicesProperties properties) {
		this.restClient = restClient;
		this.baseUrl = properties.getFidelity().getBaseUrl();
	}

	public BonusResponse awardBonus(String user, int bonus) {
		URI uri = UriComponentsBuilder.fromUri(baseUrl)
				.pathSegment("bonus")
				.build()
				.toUri();

		return restClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.body(new BonusRequest(user, bonus))
				.retrieve()
				.onStatus(status -> status.is2xxSuccessful() && status.value() != HttpStatus.CREATED.value(),
						(request, response) -> {
							throw new RestClientException("Fidelity /bonus must return HTTP 201");
						})
				.body(BonusResponse.class);
	}

	public record BonusRequest(String user, int bonus) {
	}

	public record BonusResponse(String user, int totalPoints) {
	}
}
