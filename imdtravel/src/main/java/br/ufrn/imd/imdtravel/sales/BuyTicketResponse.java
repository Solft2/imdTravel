package br.ufrn.imd.imdtravel.sales;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BuyTicketResponse(
		String transactionId,
		String flight,
		String day,
		String user,
		BigDecimal valueUSD,
		BigDecimal valueBRL,
		BigDecimal rate,
		OffsetDateTime rateAsOf,
		int bonus) {
}
