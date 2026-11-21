package br.ufrn.imd.imdtravel.observability;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import br.ufrn.imd.imdtravel.context.CorrelationIdContext;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class StructuredEventLogger {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public synchronized void log(String event, long elapsedMs) {
		Map<String, Object> fields = new LinkedHashMap<>();
		fields.put("ts", Instant.now().toString());
		fields.put("svc", "imdtravel");
		fields.put("cid", CorrelationIdContext.getCorrelationId());
		fields.put("event", event);
		fields.put("ms", elapsedMs);
		try {
			System.out.println(objectMapper.writeValueAsString(fields));
		}
		catch (JacksonException exception) {
			throw new IllegalStateException("Could not serialize structured event " + event, exception);
		}
	}
}
