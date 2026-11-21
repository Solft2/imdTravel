package br.ufrn.imd.imdtravel.sales;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleRecord(
		String correlationId,
		String transactionId,
		String flight,
		String day,
		String user,
		BigDecimal valueUSD,
		BigDecimal valueBRL,
		OffsetDateTime ts) {
}
