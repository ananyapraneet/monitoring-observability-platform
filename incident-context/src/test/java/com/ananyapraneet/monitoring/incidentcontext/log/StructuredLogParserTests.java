package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class StructuredLogParserTests {

    private final StructuredLogParser parser =
            new StructuredLogParser(
                    new ObjectMapper().findAndRegisterModules()
            );

    @Test
    void shouldParseStructuredRequestLog() {

        String json = """
                {
                  "timestamp": "2026-09-11T07:14:25.58089916Z",
                  "level": "INFO",
                  "requestId": "e906acbe-66d9-4c40-8dbd-6dc1e7fde6c9",
                  "service": "order-service",
                  "logger": "com.ananyapraneet.monitoring.orderservice.filter.RequestLoggingFilter",
                  "message": "HTTP request completed: method=GET endpoint=/orders/999999999 status=404 durationMs=665"
                }
                """;

        LogEvidence evidence = parser.parse(json);

        assertNotNull(evidence);
        assertEquals(
                Instant.parse("2026-09-11T07:14:25.58089916Z"),
                evidence.timestamp()
        );
        assertEquals("INFO", evidence.level());
        assertEquals("order-service", evidence.service());
        assertEquals(
                "e906acbe-66d9-4c40-8dbd-6dc1e7fde6c9",
                evidence.requestId()
        );
        assertEquals(
                "HTTP request completed: method=GET endpoint=/orders/999999999 status=404 durationMs=665",
                evidence.message()
        );
        assertNull(evidence.exception());
    }

    @Test
    void shouldParseExceptionField() {

        String json = """
                {
                  "timestamp": "2026-09-11T07:14:25.58089916Z",
                  "level": "ERROR",
                  "requestId": "abc123",
                  "service": "order-service",
                  "message": "Request failed",
                  "exception": "java.lang.RuntimeException: database unavailable"
                }
                """;

        LogEvidence evidence = parser.parse(json);

        assertNotNull(evidence);
        assertEquals("ERROR", evidence.level());
        assertEquals("order-service", evidence.service());
        assertEquals("abc123", evidence.requestId());
        assertEquals("Request failed", evidence.message());
        assertEquals(
                "java.lang.RuntimeException: database unavailable",
                evidence.exception()
        );
    }

    @Test
    void shouldReturnNullForMalformedJson() {

        LogEvidence evidence = parser.parse(
                "{\"timestamp\":\"invalid-json\""
        );

        assertNull(evidence);
    }

    @Test
    void shouldReturnNullWhenRequiredFieldsAreMissing() {

        String json = """
                {
                  "timestamp": "2026-09-11T07:14:25.58089916Z",
                  "level": "INFO",
                  "service": "order-service"
                }
                """;

        LogEvidence evidence = parser.parse(json);

        assertNull(evidence);
    }
}
