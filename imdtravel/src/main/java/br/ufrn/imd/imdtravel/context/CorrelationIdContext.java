package br.ufrn.imd.imdtravel.context;

public final class CorrelationIdContext {

	private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

	private CorrelationIdContext() {
	}

	public static void setCorrelationId(String correlationId) {
		CORRELATION_ID.set(correlationId);
	}

	public static String getCorrelationId() {
		return CORRELATION_ID.get();
	}

	public static void clear() {
		CORRELATION_ID.remove();
	}
}
