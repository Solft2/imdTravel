package br.ufrn.imd.imdtravel.controller;

import java.io.IOException;

import br.ufrn.imd.imdtravel.sales.BuyTicketRequest;
import br.ufrn.imd.imdtravel.sales.BuyTicketResponse;
import br.ufrn.imd.imdtravel.sales.BuyTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuyTicketController {

	private final BuyTicketService buyTicketService;

	public BuyTicketController(BuyTicketService buyTicketService) {
		this.buyTicketService = buyTicketService;
	}

	@PostMapping("/buyTicket")
	@ResponseStatus(HttpStatus.CREATED)
	public BuyTicketResponse buy(@Valid @RequestBody BuyTicketRequest request) throws IOException {
		return buyTicketService.buy(request);
	}
}
