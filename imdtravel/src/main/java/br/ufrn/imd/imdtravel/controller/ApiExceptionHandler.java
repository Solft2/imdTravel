package br.ufrn.imd.imdtravel.controller;

import br.ufrn.imd.imdtravel.sales.FlightNotFoundException;
import br.ufrn.imd.imdtravel.sales.SoldOutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class})
	public ResponseEntity<ApiError> invalidRequest(Exception exception) {
		return ResponseEntity.badRequest().body(new ApiError("invalid-request"));
	}

	@ExceptionHandler(FlightNotFoundException.class)
	public ResponseEntity<ApiError> flightNotFound(FlightNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("not-found"));
	}

	@ExceptionHandler(SoldOutException.class)
	public ResponseEntity<ApiError> soldOut(SoldOutException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiError("sold-out"));
	}

	public record ApiError(String error) {
	}
}
