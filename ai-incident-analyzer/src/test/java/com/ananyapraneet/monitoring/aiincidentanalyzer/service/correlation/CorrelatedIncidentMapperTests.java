package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedAlert;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedIncident;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MultiSignalCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.TemporalCorrelation;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelatedIncidentMapperTests {

    private final CorrelatedIncidentMapper mapper =
            new CorrelatedIncidentMapper();

    @Test
    void mapsTemporalCorrelationToIncident() {

        CorrelatedAlert alert =
                new CorrelatedAlert(
                        "PostgreSQLHighCPU",
                        "firing",
                        "postgresql",
                        "PostgreSQL CPU is high",
                        "2026-09-24T10:00:00Z"
                );

        TemporalCorrelation temporalCorrelation =
                new TemporalCorrelation(
                        List.of(
                                alert
                        ),
                        Duration.ofMinutes(5),
                        "PostgreSQLHighCPU",
                        List.of(),
                        0.0
                );

        MultiSignalCorrelation correlation =
                new MultiSignalCorrelation(
                        temporalCorrelation,
                        List.of(),
                        List.of(),
                        List.of()
                );

        CorrelatedIncident result =
                mapper.map(correlation);

        assertEquals(
                1,
                result.alerts().size()
        );

        assertEquals(
                "postgresql",
                result.primaryService()
        );

        assertTrue(
                result.correlationTypes()
                        .contains(CorrelationType.TEMPORAL)
        );

        assertTrue(
                result.affectedServices()
                        .contains("postgresql")
        );

        assertNull(
                result.likelyRootCause()
        );
    }

    @Test
    void mapsMultipleSignalsToMultiSignalIncident() {

        CorrelatedAlert alert =
                new CorrelatedAlert(
                        "OrderServiceHighLatency",
                        "firing",
                        "order-service",
                        "Order service latency is high",
                        "2026-09-24T10:00:00Z"
                );

        TemporalCorrelation temporalCorrelation =
                new TemporalCorrelation(
                        List.of(alert),
                        Duration.ofMinutes(5),
                        "OrderServiceHighLatency",
                        List.of(),
                        0.0
                );

        MetricEvidence metric =
                new MetricEvidence(
                        "http_request_duration_seconds",
                        "order-service",
                        "order-service",
                        2.5,
                        1.0,
                        Instant.parse(
                                "2026-09-24T10:00:00Z"
                        )
                );

        MetricCorrelation metricCorrelation =
                new MetricCorrelation(
                        "OrderServiceHighLatency",
                        List.of(metric),
                        0.5
                );

        LogEvidence log =
                new LogEvidence(
                        "order-service",
                        "order-service",
                        "ERROR",
                        "Database timeout",
                        Instant.parse(
                                "2026-09-24T10:00:00Z"
                        )
                );

        LogCorrelation logCorrelation =
                new LogCorrelation(
                        "OrderServiceHighLatency",
                        List.of(log),
                        0.5
                );

        MultiSignalCorrelation correlation =
                new MultiSignalCorrelation(
                        temporalCorrelation,
                        List.of(
                                "PostgreSQLHighCPU -> OrderServiceHighLatency"
                        ),
                        List.of(metricCorrelation),
                        List.of(logCorrelation)
                );

        CorrelatedIncident result =
                mapper.map(correlation);

        assertTrue(
                result.correlationTypes()
                        .contains(CorrelationType.TEMPORAL)
        );

        assertTrue(
                result.correlationTypes()
                        .contains(
                                CorrelationType.SERVICE_DEPENDENCY
                        )
        );

        assertTrue(
                result.correlationTypes()
                        .contains(CorrelationType.METRIC)
        );

        assertTrue(
                result.correlationTypes()
                        .contains(CorrelationType.LOG)
        );

        assertTrue(
                result.correlationTypes()
                        .contains(CorrelationType.MULTI_SIGNAL)
        );

        assertEquals(
                1,
                result.affectedServices().size()
        );

        assertEquals(
                "order-service",
                result.primaryService()
        );
    }

    @Test
    void returnsEmptyIncidentWhenCorrelationIsNull() {

        CorrelatedIncident result =
                mapper.map(null);

        assertEquals(
                "No correlated incident identified.",
                result.incident()
        );

        assertNull(
                result.primaryService()
        );

        assertNull(
                result.likelyRootCause()
        );

        assertTrue(
                result.alerts().isEmpty()
        );

        assertTrue(
                result.correlationTypes().isEmpty()
        );

        assertTrue(
                result.affectedServices().isEmpty()
        );
    }

    @Test
    void doesNotAddMultiSignalForSingleSignal() {

        TemporalCorrelation temporalCorrelation =
                new TemporalCorrelation(
                        List.of(),
                        Duration.ofMinutes(5),
                        null,
                        List.of(),
                        0.0
                );

        MultiSignalCorrelation correlation =
                new MultiSignalCorrelation(
                        temporalCorrelation,
                        List.of(),
                        List.of(),
                        List.of()
                );

        CorrelatedIncident result =
                mapper.map(correlation);

        assertTrue(
                result.correlationTypes().isEmpty()
        );
    }
}
