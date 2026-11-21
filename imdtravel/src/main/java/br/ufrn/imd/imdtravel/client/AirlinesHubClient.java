package br.ufrn.imd.imdtravel.client;

import java.math.BigDecimal;
import java.net.URI;

import br.ufrn.imd.imdtravel.config.ExternalServicesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class AirlinesHubClient {

	private final RestClient restClient;

	private final URI baseUrl;

	public AirlinesHubClient(RestClient restClient, ExternalServicesProperties properties) {
		this.restClient = restClient;
		this.baseUrl = properties.getAirlineshub().getBaseUrl();
	}

	public FlightResponse getFlight(String flight, String day) {
		URI uri = UriComponentsBuilder.fromUri(baseUrl)
				.pathSegment("flight")
				.queryParam("flight", flight)
				.queryParam("day", day)
				.build()
				.encode()
				.toUri();

		return restClient.get()
				.uri(uri)
				.retrieve()
				.body(FlightResponse.class);
	}

	public SellResponse sellFlight(String flight, String day) {
		URI uri = UriComponentsBuilder.fromUri(baseUrl)
				.pathSegment("sell")
				.build()
				.toUri();

		return restClient.post()
				.uri(uri)
				.contentType(MediaType.APPLICATION_JSON)
				.body(new SellRequest(flight, day))
				.retrieve()
				.onStatus(status -> status.is2xxSuccessful() && status.value() != HttpStatus.CREATED.value(),
						(request, response) -> {
							throw new RestClientException("AirlinesHub /sell must return HTTP 201");
						})
				.body(SellResponse.class);
	}

	public record FlightResponse(String flight, String day, BigDecimal priceUSD, int seatsAvailable) {
	}

	public record SellRequest(String flight, String day) {
	}

	public record SellResponse(String transactionId, int seatsRemaining) {
	}
}
