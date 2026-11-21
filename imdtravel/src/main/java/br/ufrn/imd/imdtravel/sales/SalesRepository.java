package br.ufrn.imd.imdtravel.sales;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Repository
public class SalesRepository {

	private static final TypeReference<List<SaleRecord>> SALES_TYPE = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	private final Path salesFile;

	private final List<SaleRecord> sales;

	public SalesRepository(
			ObjectMapper objectMapper,
			@Value("${sales.storage.directory:./data}") String storageDirectory) {
		this.objectMapper = objectMapper;
		this.salesFile = Path.of(storageDirectory).toAbsolutePath().normalize().resolve("sales.json");
		this.sales = loadSales();
	}

	public synchronized List<SaleRecord> findAll() {
		return List.copyOf(sales);
	}

	public synchronized void add(SaleRecord sale) {
		List<SaleRecord> updatedSales = new ArrayList<>(sales);
		updatedSales.add(sale);
		persist(updatedSales);
		sales.add(sale);
	}

	private List<SaleRecord> loadSales() {
		if (Files.notExists(salesFile)) {
			return new ArrayList<>();
		}

		try {
			return new ArrayList<>(objectMapper.readValue(salesFile.toFile(), SALES_TYPE));
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Could not load persisted sales from " + salesFile, exception);
		}
	}

	private void persist(List<SaleRecord> updatedSales) {
		Path directory = salesFile.getParent();
		Path temporaryFile = null;
		try {
			Files.createDirectories(directory);
			temporaryFile = Files.createTempFile(directory, "sales-", ".json.tmp");
			objectMapper.writeValue(temporaryFile.toFile(), updatedSales);
			moveIntoPlace(temporaryFile);
		}
		catch (IOException | JacksonException exception) {
			if (temporaryFile != null) {
				try {
					Files.deleteIfExists(temporaryFile);
				}
				catch (IOException cleanupException) {
					exception.addSuppressed(cleanupException);
				}
			}
			throw new IllegalStateException("Could not persist sales to " + salesFile, exception);
		}
	}

	private void moveIntoPlace(Path temporaryFile) throws IOException {
		try {
			Files.move(temporaryFile, salesFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (AtomicMoveNotSupportedException exception) {
			Files.move(temporaryFile, salesFile, StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
