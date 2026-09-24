package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MultiSignalCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.ServiceDependency;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MultiSignalCorrelationEngineTests {

    private final ServiceDependencyGraph dependencyGraph =
            new ServiceDependencyGraph();

    private final ServiceDependencyCorrelator
            serviceDependencyCorrelator =
            new ServiceDependencyCorrelator(
                    dependencyGraph
            );

    private final MultiSignalCorrelationEngine engine =
            new MultiSignalCorrelationEngine(
                    new TemporalCorrelator(),
                    serviceDependencyCorrelator,
                    new MetricCorrelator(),
                    new LogCorrelator()
            );

    @Test
    void combinesTemporalMetricAndLogSignals() {

        AlertEvidence firstAlert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1",
                        "2026-09-10T15:00:00Z"
                );

        AlertEvidence secondAlert =
                createAlert(
                        "PostgreSQLHighConnections",
                        "postgresql",
                        "postgres-1",
                        "2026-09-10T15:02:00Z"
                );

        MetricEvidence metric =
                createMetric(
                        "cpu_usage",
                        "postgresql",
                        "postgres-1",
                        95.0,
                        80.0
                );

        LogEvidence log =
                createLog(
                        "postgresql",
                        "postgres-1",
                        "ERROR",
                        "Connection pool exhausted"
                );

        MultiSignalCorrelation result =
                engine.correlate(
                        List.of(firstAlert, secondAlert),
                        List.of(metric),
                        List.of(log),
                        Duration.ofMinutes(5)
                );

        assertEquals(
                2,
                result.temporalCorrelation()
                        .alerts()
                        .size()
        );

        assertEquals(
                1,
                result.temporalCorrelation()
                        .symptomAlerts()
                        .size()
                );

        assertEquals(
                2,
                result.metricCorrelations()
                        .size()
        );

        assertEquals(
                2,
                result.logCorrelations()
                        .size()
        );
    }

    @Test
    void combinesServiceDependencyAndTemporalSignals() {

        dependencyGraph.addDependency(
                new ServiceDependency(
                        "postgresql",
                        "order-service"
                )
        );

        AlertEvidence databaseAlert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1",
                        "2026-09-10T15:00:00Z"
                );

        AlertEvidence serviceAlert =
                createAlert(
                        "OrderServiceHighLatency",
                        "order-service",
                        "order-1",
                        "2026-09-10T15:01:00Z"
                );

        MultiSignalCorrelation result =
                engine.correlate(
                        List.of(
                                databaseAlert,
                                serviceAlert
                        ),
                        List.of(),
                        List.of(),
                        Duration.ofMinutes(5)
                );

        assertEquals(
                1,
                result.dependencyRelationships()
                        .size()
        );

        assertEquals(
                2,
                result.temporalCorrelation()
                        .alerts()
                        .size()
        );
    }

    @Test
    void returnsEmptySignalsWhenAlertsAreEmpty() {

        MultiSignalCorrelation result =
                engine.correlate(
                        List.of(),
                        List.of(),
                        List.of(),
                        Duration.ofMinutes(5)
                );

        assertEquals(
                0,
                result.temporalCorrelation()
                        .alerts()
                        .size()
        );

        assertEquals(
                0,
                result.dependencyRelationships()
                        .size()
        );

        assertEquals(
                0,
                result.metricCorrelations()
                        .size()
        );

        assertEquals(
                0,
                result.logCorrelations()
                        .size()
        );
    }

    @Test
    void rejectsInvalidTimeWindow() {

        assertThrows(
                IllegalArgumentException.class,
                () -> engine.correlate(
                        List.of(),
                        List.of(),
                        List.of(),
                        Duration.ZERO
                )
        );
    }

    private AlertEvidence createAlert(
            String alertName,
            String service,
            String instance,
            String startsAt
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
                Instant.parse(startsAt),
                null
        );
    }

    private MetricEvidence createMetric(
            String metricName,
            String service,
            String resource,
            double value,
            double threshold
    ) {

        return new MetricEvidence(
                metricName,
                service,
                resource,
                value,
                threshold,
                Instant.parse(
                        "2026-09-10T15:00:30Z"
                )
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
                        "2026-09-10T15:00:45Z"
                )
        );
    }
}
