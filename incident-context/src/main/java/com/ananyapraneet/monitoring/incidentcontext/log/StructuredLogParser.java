package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class StructuredLogParser {

    private final ObjectMapper objectMapper;

    public StructuredLogParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LogEvidence parse(String json) {

        try {
            JsonNode root = objectMapper.readTree(json);

            Instant timestamp = parseTimestamp(root.path("timestamp").asText(null));

            String level = root.path("level").asText(null);
            String service = root.path("service").asText(null);
            String requestId = root.path("requestId").asText(null);
            String message = root.path("message").asText(null);
            String exception = root.path("exception").asText(null);

            if (timestamp == null || level == null || service == null || message == null) {
                return null;
            }

            if (exception != null && exception.isBlank()) {
                exception = null;
            }

            return new LogEvidence(
                    timestamp,
                    level,
                    service,
                    requestId,
                    message,
                    exception
            );

        } catch (Exception exception) {
            return null;
        }
    }

    private Instant parseTimestamp(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }
}
