package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogCorrelatorTests {

    private final LogCorrelator correlator =
            new LogCorrelator();

    @Test
    void correlatesErrorLogForSameServiceAndResource() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence log =
                createLog(
                        "order-service",
                        "order-1",
                        "ERROR",
                        "Database connection pool exhausted"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(log)
                );

        assertEquals(1, result.relatedLogs().size());
        assertEquals(0.5, result.correlationScore());
    }

    @Test
    void correlatesWarningLog() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence log =
                createLog(
                        "order-service",
                        "order-1",
                        "WARN",
                        "Database response time is high"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(log)
                );

        assertEquals(1, result.relatedLogs().size());
        assertEquals(0.5, result.correlationScore());
    }

    @Test
    void ignoresInfoLog() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence log =
                createLog(
                        "order-service",
                        "order-1",
                        "INFO",
                        "Request completed successfully"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(log)
                );

        assertEquals(0, result.relatedLogs().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void ignoresLogForDifferentService() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence log =
                createLog(
                        "postgresql",
                        "postgres-1",
                        "ERROR",
                        "Connection pool exhausted"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(log)
                );

        assertEquals(0, result.relatedLogs().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void ignoresLogForDifferentResource() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence log =
                createLog(
                        "order-service",
                        "order-2",
                        "ERROR",
                        "Database connection failed"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(log)
                );

        assertEquals(0, result.relatedLogs().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void correlatesMultipleLogs() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogEvidence firstLog =
                createLog(
                        "order-service",
                        "order-1",
                        "ERROR",
                        "Database connection pool exhausted"
                );

        LogEvidence secondLog =
                createLog(
                        "order-service",
                        "order-1",
                        "ERROR",
                        "Request timeout while querying PostgreSQL"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(firstLog, secondLog)
                );

        assertEquals(2, result.relatedLogs().size());
        assertEquals(0.75, result.correlationScore());
    }

    @Test
    void returnsEmptyCorrelationForEmptyLogs() {

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1"
                );

        LogCorrelation result =
                correlator.correlate(
                        alert,
                        List.of()
                );

        assertEquals(0, result.relatedLogs().size());
        assertEquals(0.0, result.correlationScore());
    }

    private AlertEvidence createAlert(
            String alertName,
            String service,
            String instance
    ) {

        return new AlertEvidence(
                alertName,
                "firing",
                "critical",
                service,
                instance,
                "Test alert",
                "Test description",
                "Test runbook",
                Map.of(),
                Instant.parse(
                        "2026-09-10T15:00:00Z"
                ),
                null
        );
    }

    private LogEvidence createLog(
            String service,
            String resource,
            String level,
            String message
    ) {

        return new LogEvidence(
                service,
                resource,
                level,
                message,
                Instant.parse(
                        "2026-09-10T15:00:00Z"
                )
        );
    }
}
