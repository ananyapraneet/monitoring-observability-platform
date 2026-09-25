package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class LogNormalizationServiceTests {

    private final LogNormalizationService service =
            new LogNormalizationService(
                    new ErrorPatternNormalizer()
            );

    @Test
    void normalizesMessageAndDetectsErrorPattern() {
        Instant timestamp = Instant.parse(
                "2026-09-25T10:15:30Z"
        );

        NormalizedLog result = service.normalize(
                "order-service",
                LogSeverity.ERROR,
                "  Connection Pool Exhausted  ",
                timestamp
        );

        assertEquals(
                "order-service",
                result.service()
        );

        assertEquals(
                LogSeverity.ERROR,
                result.severity()
        );

        assertEquals(
                "  Connection Pool Exhausted  ",
                result.originalMessage()
        );

        assertEquals(
                "connection pool exhausted",
                result.normalizedMessage()
        );

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result.errorPattern()
        );

        assertEquals(
                timestamp,
                result.timestamp()
        );
    }

    @Test
    void preservesUnknownErrorPattern() {
        NormalizedLog result = service.normalize(
                "order-service",
                LogSeverity.WARN,
                "Something unexpected happened",
                Instant.parse(
                        "2026-09-25T10:15:30Z"
                )
        );

        assertEquals(
                ErrorPattern.UNKNOWN,
                result.errorPattern()
        );

        assertEquals(
                "something unexpected happened",
                result.normalizedMessage()
        );
    }

    @Test
    void handlesNullMessage() {
        NormalizedLog result = service.normalize(
                "order-service",
                LogSeverity.ERROR,
                null,
                Instant.parse(
                        "2026-09-25T10:15:30Z"
                )
        );

        assertEquals(
                "",
                result.normalizedMessage()
        );

        assertEquals(
                ErrorPattern.UNKNOWN,
                result.errorPattern()
        );
    }

    @Test
    void trimsAndLowercasesMessage() {
        NormalizedLog result = service.normalize(
                "payment-service",
                LogSeverity.ERROR,
                "   REQUEST TIMED OUT   ",
                Instant.parse(
                        "2026-09-25T10:15:30Z"
                )
        );

        assertEquals(
                "request timed out",
                result.normalizedMessage()
        );

        assertEquals(
                ErrorPattern.TIMEOUT,
                result.errorPattern()
        );
    }

    @Test
    void rejectsNullErrorPatternNormalizer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new LogNormalizationService(null)
        );
    }
}
