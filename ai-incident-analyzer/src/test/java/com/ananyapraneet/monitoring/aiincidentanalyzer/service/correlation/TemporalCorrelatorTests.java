package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.TemporalCorrelation;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TemporalCorrelatorTests {

    private final TemporalCorrelator correlator =
            new TemporalCorrelator();

    @Test
    void correlatesAlertsWithinTimeWindow() {

        AlertEvidence first = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "2026-09-10T15:00:00Z"
        );

        AlertEvidence second = alert(
                "PostgreSQLHighConnections",
                "postgres",
                "2026-09-10T15:00:10Z"
        );

        AlertEvidence third = alert(
                "OrderServiceHighLatency",
                "order-service",
                "2026-09-10T15:00:20Z"
        );

        TemporalCorrelation result =
                correlator.correlate(
                        List.of(first, second, third),
                        Duration.ofSeconds(30)
                );

        assertEquals(3, result.alerts().size());
        assertEquals(
                "PostgreSQLHighCPU",
                result.parentAlert()
        );
        assertEquals(
                List.of(
                        "PostgreSQLHighConnections",
                        "OrderServiceHighLatency"
                ),
                result.symptomAlerts()
        );
    }

    @Test
    void excludesAlertsOutsideTimeWindow() {

        AlertEvidence first = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "2026-09-10T15:00:00Z"
        );

        AlertEvidence second = alert(
                "OrderServiceHighLatency",
                "order-service",
                "2026-09-10T15:01:00Z"
        );

        TemporalCorrelation result =
                correlator.correlate(
                        List.of(first, second),
                        Duration.ofSeconds(30)
                );

        assertEquals(1, result.alerts().size());
        assertEquals(
                "PostgreSQLHighCPU",
                result.parentAlert()
        );
        assertEquals(0, result.symptomAlerts().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void sortsAlertsChronologically() {

        AlertEvidence later = alert(
                "OrderServiceHighLatency",
                "order-service",
                "2026-09-10T15:00:20Z"
        );

        AlertEvidence earlier = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "2026-09-10T15:00:00Z"
        );

        TemporalCorrelation result =
                correlator.correlate(
                        List.of(later, earlier),
                        Duration.ofSeconds(30)
                );

        assertEquals(
                "PostgreSQLHighCPU",
                result.parentAlert()
        );

        assertEquals(
                "OrderServiceHighLatency",
                result.symptomAlerts().get(0)
        );
    }

    @Test
    void ignoresAlertsWithoutTimestamp() {

        AlertEvidence timestamped = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "2026-09-10T15:00:00Z"
        );

        AlertEvidence withoutTimestamp =
                new AlertEvidence(
                        "OrderServiceHighLatency",
                        "firing",
                        "critical",
                        "order-service",
                        "order-service:8080",
                        "Test summary",
                        "Test description",
                        "Test runbook",
                        Map.of(),
                        null,
                        null
                );

        TemporalCorrelation result =
                correlator.correlate(
                        List.of(
                                timestamped,
                                withoutTimestamp
                        ),
                        Duration.ofSeconds(30)
                );

        assertEquals(1, result.alerts().size());
    }

    @Test
    void handlesEmptyInput() {

        TemporalCorrelation result =
                correlator.correlate(
                        List.of(),
                        Duration.ofSeconds(30)
                );

        assertEquals(0, result.alerts().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void rejectsInvalidTimeWindow() {

        assertThrows(
                IllegalArgumentException.class,
                () -> correlator.correlate(
                        List.of(),
                        Duration.ZERO
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> correlator.correlate(
                        List.of(),
                        Duration.ofSeconds(-1)
                )
        );
    }

    private AlertEvidence alert(
            String alertName,
            String service,
            String startsAt
    ) {

        return new AlertEvidence(
                alertName,
                "firing",
                "critical",
                service,
                service + ":8080",
                "Test summary",
                "Test description",
                "Test runbook",
                Map.of(),
                Instant.parse(startsAt),
                null
        );
    }
}
