package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricCorrelatorTests {

    private final MetricCorrelator correlator =
            new MetricCorrelator();

    @Test
    void correlatesMetricForSameServiceAndResource() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1"
                );

        MetricEvidence metric =
                createMetric(
                        "cpu_usage",
                        "postgresql",
                        "postgres-1",
                        95.0,
                        80.0
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(metric)
                );

        assertEquals(1, result.relatedMetrics().size());
        assertEquals(0.5, result.correlationScore());
    }

    @Test
    void ignoresMetricForDifferentService() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1"
                );

        MetricEvidence metric =
                createMetric(
                        "cpu_usage",
                        "order-service",
                        "order-1",
                        95.0,
                        80.0
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(metric)
                );

        assertEquals(0, result.relatedMetrics().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void ignoresMetricForDifferentResource() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1"
                );

        MetricEvidence metric =
                createMetric(
                        "cpu_usage",
                        "postgresql",
                        "postgres-2",
                        95.0,
                        80.0
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(metric)
                );

        assertEquals(0, result.relatedMetrics().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void ignoresMetricWhenThresholdIsNotBreached() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1"
                );

        MetricEvidence metric =
                createMetric(
                        "cpu_usage",
                        "postgresql",
                        "postgres-1",
                        70.0,
                        80.0
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(metric)
                );

        assertEquals(0, result.relatedMetrics().size());
        assertEquals(0.0, result.correlationScore());
    }

    @Test
    void correlatesMultipleMetrics() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLResourcePressure",
                        "postgresql",
                        "postgres-1"
                );

        MetricEvidence cpu =
                createMetric(
                        "cpu_usage",
                        "postgresql",
                        "postgres-1",
                        95.0,
                        80.0
                );

        MetricEvidence memory =
                createMetric(
                        "memory_usage",
                        "postgresql",
                        "postgres-1",
                        90.0,
                        80.0
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of(cpu, memory)
                );

        assertEquals(2, result.relatedMetrics().size());
        assertEquals(0.75, result.correlationScore());
    }

    @Test
    void returnsEmptyCorrelationForEmptyMetrics() {

        AlertEvidence alert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "postgresql",
                        "postgres-1"
                );

        MetricCorrelation result =
                correlator.correlate(
                        alert,
                        List.of()
                );

        assertEquals(0, result.relatedMetrics().size());
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
                        "2026-09-10T15:00:00Z"
                )
        );
    }
}
