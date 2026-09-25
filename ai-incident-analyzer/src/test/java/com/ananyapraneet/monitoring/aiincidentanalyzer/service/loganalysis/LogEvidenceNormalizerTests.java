package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class LogEvidenceNormalizerTests {

    private final ErrorEvidenceNormalizerHolder holder =
            new ErrorEvidenceNormalizerHolder();

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void convertsLogEvidenceToNormalizedLog() {

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "request-123",
                        "Connection pool exhausted",
                        null
                );

        List<NormalizedLog> result =
                holder.normalizer.normalize(
                        List.of(log)
                );

        assertEquals(1, result.size());

        NormalizedLog normalized =
                result.get(0);

        assertEquals(
                "order-service",
                normalized.service()
        );

        assertEquals(
                LogSeverity.ERROR,
                normalized.severity()
        );

        assertEquals(
                "Connection pool exhausted",
                normalized.originalMessage()
        );

        assertEquals(
                "connection pool exhausted",
                normalized.normalizedMessage()
        );

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                normalized.errorPattern()
        );

        assertEquals(
                timestamp,
                normalized.timestamp()
        );
    }

    @Test
    void includesExceptionWhenNormalizingLog() {

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "request-123",
                        "Failed to acquire JDBC connection",
                        "SQLTransientConnectionException: connection timed out"
                );

        List<NormalizedLog> result =
                holder.normalizer.normalize(
                        List.of(log)
                );

        assertEquals(1, result.size());

        NormalizedLog normalized =
                result.get(0);

        assertTrue(
                normalized.originalMessage().contains(
                        "Failed to acquire JDBC connection"
                )
        );

        assertTrue(
                normalized.originalMessage().contains(
                        "SQLTransientConnectionException"
                )
        );

        assertEquals(
                ErrorPattern.DATABASE_CONNECTION_TIMEOUT,
                normalized.errorPattern()
        );
    }

    @Test
    void handlesUnknownLogLevel() {

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "NOTICE",
                        "order-service",
                        "request-123",
                        "Something happened",
                        null
                );

        List<NormalizedLog> result =
                holder.normalizer.normalize(
                        List.of(log)
                );

        assertEquals(
                LogSeverity.INFO,
                result.get(0).severity()
        );
    }

    @Test
    void handlesMissingLogLevel() {

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        null,
                        "order-service",
                        "request-123",
                        "Something happened",
                        null
                );

        List<NormalizedLog> result =
                holder.normalizer.normalize(
                        List.of(log)
                );

        assertEquals(
                LogSeverity.INFO,
                result.get(0).severity()
        );
    }

    @Test
    void handlesEmptyInput() {

        List<NormalizedLog> result =
                holder.normalizer.normalize(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesNullInput() {

        List<NormalizedLog> result =
                holder.normalizer.normalize(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresNullLogEntries() {

        List<NormalizedLog> result =
                holder.normalizer.normalize(
                        java.util.Arrays.asList(
                                null,
                                new LogEvidence(
                                        timestamp,
                                        "ERROR",
                                        "order-service",
                                        "request-123",
                                        "Connection pool exhausted",
                                        null
                                )
                        )
                );

        assertEquals(1, result.size());

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result.get(0).errorPattern()
        );
    }

    private static class ErrorEvidenceNormalizerHolder {

        private final LogEvidenceNormalizer normalizer =
                new LogEvidenceNormalizer(
                        new ErrorPatternNormalizer()
                );
    }
}
