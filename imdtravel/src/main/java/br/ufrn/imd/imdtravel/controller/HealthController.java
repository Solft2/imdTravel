package br.ufrn.imd.imdtravel.controller;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

	private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

	@GetMapping("/health")
	public HealthResponse health() {
		long uptimeSeconds = runtimeMXBean.getUptime() / 1000;
		return new HealthResponse("ok", uptimeSeconds);
	}

	public record HealthResponse(String status, long uptimeS) {
	}
}