package br.ufrn.imd.imdtravel.sales;

public class SoldOutException extends RuntimeException {

	public SoldOutException(Throwable cause) {
		super(cause);
	}
}
