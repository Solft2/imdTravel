package br.ufrn.imd.imdtravel.controller;

import br.ufrn.imd.imdtravel.service.PurchaseService;
import br.ufrn.imd.imdtravel.service.PurchaseService.PurchaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PurchaseController {

	private final PurchaseService purchaseService;

	public PurchaseController(PurchaseService purchaseService) {
		this.purchaseService = purchaseService;
	}

	@PostMapping("/buyTicket")
	public ResponseEntity<PurchaseResponse> buyTicket(@Valid @RequestBody PurchaseRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(purchaseService.buyTicket(request.flight(), request.day(), request.user()));
	}

	public record PurchaseRequest(
			@NotBlank String flight,
			@NotBlank String day,
			@NotBlank String user) {
	}
}
