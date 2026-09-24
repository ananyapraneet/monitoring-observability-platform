package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedIncident;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IncidentContextCorrelationAdapterTests {

    @Test
    void returnsEmptyIncidentWhenContextIsNull() {

        IncidentContextCorrelationAdapter adapter =
                new IncidentContextCorrelationAdapter(
                        new MultiSignalCorrelationEngine(
                                new TemporalCorrelator(),
                                new ServiceDependencyCorrelator(
                                        new ServiceDependencyGraph()
                                ),
                                new MetricCorrelator(),
                                new LogCorrelator()
                        ),
                        new CorrelatedIncidentMapper()
                );

        CorrelatedIncident result =
                adapter.correlate(null);

        assertNotNull(result);
        assertEquals(List.of(), result.alerts());
        assertEquals(List.of(), result.correlationTypes());
        assertEquals(List.of(), result.affectedServices());
    }

    @Test
    void mapsAlertsAndLogsFromIncidentContext() {

        AlertEvidence alert = new AlertEvidence(
                "OrderServiceHighLatency",
                "firing",
                "critical",
                "order-service",
                "order-service-1",
                "Order service latency is high",
                "Order service latency exceeded threshold",
                "",
                Map.of(),
                Instant.parse("2026-09-24T10:00:00Z"),
                null
        );

        LogEvidence log = new LogEvidence(
                Instant.parse("2026-09-24T10:01:00Z"),
                "ERROR",
                "order-service",
                "request-123",
                "Database connection failed",
                "SQLException"
        );

        IncidentContext context = new IncidentContext(
                "order-service-latency",
                "critical",
                "order-service",
                List.of(alert),
                Map.of(),
                List.of(log),
                null,
                List.of(),
                List.of()
        );

        IncidentContextCorrelationAdapter adapter =
                new IncidentContextCorrelationAdapter(
                        new MultiSignalCorrelationEngine(
                                new TemporalCorrelator(),
                                new ServiceDependencyCorrelator(
                                        new ServiceDependencyGraph()
                                ),
                                new MetricCorrelator(),
                                new LogCorrelator()
                        ),
                        new CorrelatedIncidentMapper()
                );

        CorrelatedIncident result =
                adapter.correlate(context);

        assertNotNull(result);

        assertEquals(
                List.of("order-service"),
                result.affectedServices()
        );

        assertEquals(
                List.of(
                        com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType
                                .TEMPORAL,
                        com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType
                                .LOG,
                        com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType
                                .MULTI_SIGNAL
                ),
                result.correlationTypes()
        );

        assertEquals(1, result.alerts().size());
    }

    @Test
    void ignoresMetricsWhenIncidentContextContainsUnstructuredMetrics() {

        AlertEvidence alert = new AlertEvidence(
                "ApiGateway5xx",
                "firing",
                "critical",
                "api-gateway",
                "api-gateway-1",
                "API gateway 5xx errors",
                "",
                "",
                Map.of(),
                Instant.parse("2026-09-24T10:00:00Z"),
                null
        );

        IncidentContext context = new IncidentContext(
                "api-gateway-errors",
                "critical",
                "api-gateway",
                List.of(alert),
                Map.of(
                        "cpu_usage", 92.5,
                        "request_rate", 1500
                ),
                List.of(),
                null,
                List.of(),
                List.of()
        );

        IncidentContextCorrelationAdapter adapter =
                new IncidentContextCorrelationAdapter(
                        new MultiSignalCorrelationEngine(
                                new TemporalCorrelator(),
                                new ServiceDependencyCorrelator(
                                        new ServiceDependencyGraph()
                                ),
                                new MetricCorrelator(),
                                new LogCorrelator()
                        ),
                        new CorrelatedIncidentMapper()
                );

        CorrelatedIncident result =
                adapter.correlate(context);

        assertNotNull(result);

        assertEquals(
                List.of(
                        com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType
                                .TEMPORAL
                ),
                result.correlationTypes()
        );
    }
}
