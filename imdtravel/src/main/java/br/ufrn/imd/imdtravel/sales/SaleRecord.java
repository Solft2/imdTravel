package br.ufrn.imd.imdtravel.sales;

import java.math.BigDecimal;

public record SaleRecord(
		String correlationId,
		String transactionId,
		String flight,
		String day,
		String user,
		BigDecimal valueUSD,
		BigDecimal valueBRL,
		String ts) {
}
