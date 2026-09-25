package com.ananyapraneet.monitoring.incidentcontext.store;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLogEvidenceStoreTests {

    @Test
    void shouldStoreAndRetrieveLogsByService() {
        InMemoryLogEvidenceStore store =
                new InMemoryLogEvidenceStore();

        LogEvidence orderLog = new LogEvidence(
                Instant.parse("2026-09-25T10:00:00Z"),
                "ERROR",
                "order-service",
                "request-1",
                "Database connection failed",
                "java.sql.SQLException"
        );

        LogEvidence userLog = new LogEvidence(
                Instant.parse("2026-09-25T10:00:01Z"),
                "ERROR",
                "user-service",
                "request-2",
                "Database connection failed",
                "java.sql.SQLException"
        );

        store.add(orderLog);
        store.add(userLog);

        List<LogEvidence> result =
                store.getByService("order-service");

        assertEquals(1, result.size());
        assertEquals(orderLog, result.get(0));
    }

    @Test
    void shouldReturnEmptyListForUnknownService() {
        InMemoryLogEvidenceStore store =
                new InMemoryLogEvidenceStore();

        assertTrue(
                store.getByService("order-service").isEmpty()
        );
    }

    @Test
    void shouldIgnoreNullLog() {
        InMemoryLogEvidenceStore store =
                new InMemoryLogEvidenceStore();

        store.add(null);

        assertTrue(
                store.getByService("order-service").isEmpty()
        );
    }

    @Test
    void shouldReturnEmptyListForBlankService() {
        InMemoryLogEvidenceStore store =
                new InMemoryLogEvidenceStore();

        assertTrue(store.getByService(null).isEmpty());
        assertTrue(store.getByService("").isEmpty());
        assertTrue(store.getByService("   ").isEmpty());
    }

    @Test
    void shouldKeepOnlyMostRecentThousandLogs() {
        InMemoryLogEvidenceStore store =
                new InMemoryLogEvidenceStore();

        for (int i = 0; i < 1001; i++) {
            LogEvidence log = new LogEvidence(
                    Instant.parse("2026-09-25T10:00:00Z")
                            .plusSeconds(i),
                    "INFO",
                    "order-service",
                    "request-" + i,
                    "Log message " + i,
                    null
            );

            store.add(log);
        }

        List<LogEvidence> result =
                store.getByService("order-service");

        assertEquals(1000, result.size());
        assertEquals(
                "request-1",
                result.get(0).requestId()
        );
        assertEquals(
                "request-1000",
                result.get(999).requestId()
        );
    }
}
