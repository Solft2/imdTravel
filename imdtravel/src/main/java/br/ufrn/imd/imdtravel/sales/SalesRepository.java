package br.ufrn.imd.imdtravel.sales;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class SalesRepository {

	private final Path salesDirectory;
	private final Path salesFile;
	private final JsonMapper jsonMapper;

	public SalesRepository(
			JsonMapper jsonMapper,
			@Value("${SALES_DIR:./data}") String salesDirectory) throws IOException {
		this.jsonMapper = jsonMapper;
		this.salesDirectory = Path.of(salesDirectory).toAbsolutePath().normalize();
		Files.createDirectories(this.salesDirectory);
		this.salesFile = this.salesDirectory.resolve("sales.jsonl");
	}

	public synchronized void add(SaleRecord sale) throws IOException {
		String line = jsonMapper.writeValueAsString(sale) + System.lineSeparator();
		Files.writeString(
				salesFile,
				line,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.WRITE,
				StandardOpenOption.APPEND);
	}

	public synchronized List<SaleRecord> findAll() throws IOException {
		if (!Files.exists(salesFile)) {
			return List.of();
		}

		List<SaleRecord> sales = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(salesFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.isBlank()) {
					sales.add(jsonMapper.readValue(line, SaleRecord.class));
				}
			}
		}
		return List.copyOf(sales);
	}

	public boolean isAvailable() {
		return Files.isDirectory(salesDirectory) && Files.isWritable(salesDirectory);
	}
}
