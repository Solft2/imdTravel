package br.ufrn.imd.imdtravel.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import br.ufrn.imd.imdtravel.sales.SalesRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

	private final SalesRepository salesRepository;

	public HealthController(SalesRepository salesRepository) {
		this.salesRepository = salesRepository;
	}

	@GetMapping("/health")
	public ResponseEntity<HealthResponse> health() {
		long uptimeSeconds = runtimeMXBean.getUptime() / 1000;
		if (!salesRepository.isAvailable()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new HealthResponse("down", uptimeSeconds, "sales storage is not writable"));
		}
		return ResponseEntity.ok(new HealthResponse("ok", uptimeSeconds, null));
	}

	public record HealthResponse(String status, long uptimeS, String reason) {
	}
}