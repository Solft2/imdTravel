package br.ufrn.imd.imdtravel.controller;

import java.util.Map;

import br.ufrn.imd.imdtravel.observability.StructuredEventLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import br.ufrn.imd.imdtravel.service.FlightNotFoundException;
import br.ufrn.imd.imdtravel.service.SoldOutException;

@RestControllerAdvice
public class ApiExceptionHandler {

	private final StructuredEventLogger eventLogger;

	public ApiExceptionHandler(StructuredEventLogger eventLogger) {
		this.eventLogger = eventLogger;
	}

	@ExceptionHandler(SoldOutException.class)
	public ResponseEntity<Map<String, String>> handleConflict(SoldOutException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "sold-out"));
	}

	@ExceptionHandler(FlightNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(FlightNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "not-found"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleInvalidRequest(MethodArgumentNotValidException exception) {
		return ResponseEntity.badRequest().body(Map.of("error", "invalid-request"));
	}

	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<Map<String, String>> handleDependencyFailure(RestClientException exception) {
		eventLogger.log("dependency.failed", 0);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "dependency-failure"));
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleInternalFailure(IllegalStateException exception) {
		eventLogger.log("request.failed", 0);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("error", "internal-error"));
	}
}
