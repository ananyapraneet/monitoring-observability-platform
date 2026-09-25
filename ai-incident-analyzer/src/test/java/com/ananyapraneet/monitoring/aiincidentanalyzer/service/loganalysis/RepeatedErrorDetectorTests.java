package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RepeatedError;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class RepeatedErrorDetectorTests {

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void detectsRepeatedErrorPattern() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(3);

        List<NormalizedLog> logs =
                List.of(
                        normalizedLog(
                                "Connection pool exhausted",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED
                        ),
                        normalizedLog(
                                "Connection pool exhausted for order-db",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED
                        ),
                        normalizedLog(
                                "Connection pool exhausted for user-db",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED
                        ),
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        )
                );

        List<RepeatedError> result =
                detector.detect(logs);

        assertEquals(1, result.size());

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result.get(0).errorPattern()
        );

        assertEquals(
                3,
                result.get(0).occurrenceCount()
        );
    }

    @Test
    void detectsMultipleRepeatedErrorPatterns() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(2);

        List<NormalizedLog> logs =
                List.of(
                        normalizedLog(
                                "Connection pool exhausted",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED
                        ),
                        normalizedLog(
                                "Connection pool exhausted",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED
                        ),
                        normalizedLog(
                                "HTTP 500 Internal Server Error",
                                ErrorPattern.HTTP_5XX
                        ),
                        normalizedLog(
                                "HTTP 500 Internal Server Error",
                                ErrorPattern.HTTP_5XX
                        ),
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        )
                );

        List<RepeatedError> result =
                detector.detect(logs);

        assertEquals(2, result.size());

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result.get(0).errorPattern()
        );

        assertEquals(
                2,
                result.get(0).occurrenceCount()
        );

        assertEquals(
                ErrorPattern.HTTP_5XX,
                result.get(1).errorPattern()
        );

        assertEquals(
                2,
                result.get(1).occurrenceCount()
        );
    }

    @Test
    void detectsErrorExactlyAtThreshold() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(3);

        List<NormalizedLog> logs =
                List.of(
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        ),
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        ),
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        )
                );

        List<RepeatedError> result =
                detector.detect(logs);

        assertEquals(1, result.size());

        assertEquals(
                ErrorPattern.TIMEOUT,
                result.get(0).errorPattern()
        );

        assertEquals(
                3,
                result.get(0).occurrenceCount()
        );
    }

    @Test
    void ignoresPatternBelowThreshold() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(3);

        List<NormalizedLog> logs =
                List.of(
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        ),
                        normalizedLog(
                                "Request timed out",
                                ErrorPattern.TIMEOUT
                        )
                );

        List<RepeatedError> result =
                detector.detect(logs);

        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresUnknownPatterns() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(2);

        List<NormalizedLog> logs =
                List.of(
                        normalizedLog(
                                "Something unexpected happened",
                                ErrorPattern.UNKNOWN
                        ),
                        normalizedLog(
                                "Something unexpected happened again",
                                ErrorPattern.UNKNOWN
                        )
                );

        List<RepeatedError> result =
                detector.detect(logs);

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesEmptyLogs() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(2);

        List<RepeatedError> result =
                detector.detect(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void handlesNullLogs() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(2);

        List<RepeatedError> result =
                detector.detect(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresNullLogEntries() {

        RepeatedErrorDetector detector =
                new RepeatedErrorDetector(2);

        NormalizedLog timeout =
                normalizedLog(
                        "Request timed out",
                        ErrorPattern.TIMEOUT
                );

        List<NormalizedLog> logs =
                java.util.Arrays.asList(null, timeout);

        List<RepeatedError> result =
                detector.detect(logs);

        assertTrue(result.isEmpty());
    }

    @Test
    void rejectsThresholdBelowTwo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new RepeatedErrorDetector(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new RepeatedErrorDetector(0)
        );
    }

    private NormalizedLog normalizedLog(
            String message,
            ErrorPattern errorPattern) {

        return new NormalizedLog(
                "order-service",
                LogSeverity.ERROR,
                message,
                message.toLowerCase(),
                errorPattern,
                timestamp
        );
    }
}
