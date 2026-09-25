package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class NormalizedLogTests {

    @Test
    void createsNormalizedLogWithAllFields() {
        Instant timestamp = Instant.parse("2026-09-25T10:15:30Z");

        NormalizedLog log = new NormalizedLog(
                "order-service",
                LogSeverity.ERROR,
                "Connection pool exhausted",
                "connection pool exhausted",
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                timestamp
        );

        assertEquals("order-service", log.service());
        assertEquals(LogSeverity.ERROR, log.severity());
        assertEquals("Connection pool exhausted", log.originalMessage());
        assertEquals("connection pool exhausted", log.normalizedMessage());
        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                log.errorPattern()
        );
        assertEquals(timestamp, log.timestamp());
    }

    @Test
    void allowsLogWithoutKnownErrorPattern() {
        NormalizedLog log = new NormalizedLog(
                "order-service",
                LogSeverity.WARN,
                "Unexpected warning",
                "unexpected warning",
                null,
                Instant.parse("2026-09-25T10:15:30Z")
        );

        assertNull(log.errorPattern());
    }
}
