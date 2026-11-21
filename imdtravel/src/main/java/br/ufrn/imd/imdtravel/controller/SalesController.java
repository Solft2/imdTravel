package br.ufrn.imd.imdtravel.controller;

import java.util.List;

import br.ufrn.imd.imdtravel.sales.SaleRecord;
import br.ufrn.imd.imdtravel.sales.SalesRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SalesController {

	private final SalesRepository salesRepository;

	public SalesController(SalesRepository salesRepository) {
		this.salesRepository = salesRepository;
	}

	@GetMapping("/sales")
	public List<SaleRecord> sales() {
		return salesRepository.findAll();
	}
}
