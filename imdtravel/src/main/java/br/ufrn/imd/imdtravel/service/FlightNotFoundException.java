package br.ufrn.imd.imdtravel.service;

public class FlightNotFoundException extends RuntimeException {

	public FlightNotFoundException(Throwable cause) {
		super("Flight was not found", cause);
	}
}
