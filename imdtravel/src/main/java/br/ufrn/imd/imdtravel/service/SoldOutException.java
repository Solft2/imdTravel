package br.ufrn.imd.imdtravel.service;

public class SoldOutException extends RuntimeException {

	public SoldOutException(Throwable cause) {
		super("Flight has no seats available", cause);
	}
}
