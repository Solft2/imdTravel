package br.ufrn.imd.imdtravel.config;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "services")
public class ExternalServicesProperties {

	private final Service airlineshub = new Service();

	private final Service exchange = new Service();

	private final Service fidelity = new Service();

	public Service getAirlineshub() {
		return airlineshub;
	}

	public Service getExchange() {
		return exchange;
	}

	public Service getFidelity() {
		return fidelity;
	}

	public static class Service {

		private URI baseUrl;

		public URI getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(URI baseUrl) {
			this.baseUrl = baseUrl;
		}
	}
}
