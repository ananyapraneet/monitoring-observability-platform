package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.TimelineEvent;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricSample;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricsClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentContextAnomalyEnricherTests {

    @Test
    void addsAnomalousApplicationMetrics() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        fakeService.addResult(
                AnomalyMetricType.REQUEST_RATE,
                new AnomalyResult(
                        AnomalyMetricType.REQUEST_RATE,
                        "order-service",
                        100.0,
                        20.0,
                        10.0,
                        8.0,
                        true
                )
        );

        fakeService.addResult(
                AnomalyMetricType.LATENCY,
                new AnomalyResult(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        2.0,
                        0.2,
                        0.1,
                        18.0,
                        true
                )
        );

        fakeService.addResult(
                AnomalyMetricType.ERROR_RATE,
                new AnomalyResult(
                        AnomalyMetricType.ERROR_RATE,
                        "order-service",
                        0.2,
                        0.01,
                        0.02,
                        9.5,
                        true
                )
        );

        IncidentContext context =
                createContext("order-service");

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        IncidentContext enrichedContext =
                enricher.enrich(context);

        assertNotNull(enrichedContext);
        assertEquals(3, enrichedContext.anomalies().size());

        assertTrue(
                enrichedContext.anomalies()
                        .stream()
                        .anyMatch(anomaly ->
                                anomaly.metricType()
                                        == AnomalyMetricType.REQUEST_RATE)
        );

        assertTrue(
                enrichedContext.anomalies()
                        .stream()
                        .anyMatch(anomaly ->
                                anomaly.metricType()
                                        == AnomalyMetricType.LATENCY)
        );

        assertTrue(
                enrichedContext.anomalies()
                        .stream()
                        .anyMatch(anomaly ->
                                anomaly.metricType()
                                        == AnomalyMetricType.ERROR_RATE)
        );
    }

    @Test
    void ignoresNormalApplicationMetrics() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        fakeService.addResult(
                AnomalyMetricType.REQUEST_RATE,
                normalResult(
                        AnomalyMetricType.REQUEST_RATE,
                        "order-service"
                )
        );

        fakeService.addResult(
                AnomalyMetricType.LATENCY,
                normalResult(
                        AnomalyMetricType.LATENCY,
                        "order-service"
                )
        );

        fakeService.addResult(
                AnomalyMetricType.ERROR_RATE,
                normalResult(
                        AnomalyMetricType.ERROR_RATE,
                        "order-service"
                )
        );

        IncidentContext context =
                createContext("order-service");

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        IncidentContext enrichedContext =
                enricher.enrich(context);

        assertNotNull(enrichedContext);
        assertTrue(enrichedContext.anomalies().isEmpty());
    }

    @Test
    void addsDatabaseConnectionAnomalyForPostgresql() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        fakeService.addResult(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                new AnomalyResult(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        "postgresql",
                        0.95,
                        0.40,
                        0.05,
                        11.0,
                        true
                )
        );

        IncidentContext context =
                createContext("postgresql");

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        IncidentContext enrichedContext =
                enricher.enrich(context);

        assertEquals(1, enrichedContext.anomalies().size());

        assertEquals(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                enrichedContext.anomalies()
                        .get(0)
                        .metricType()
        );
    }

    @Test
    void doesNotQueryPrometheusForUnknownService() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        IncidentContext context =
                createContext("unknown");

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        IncidentContext enrichedContext =
                enricher.enrich(context);

        assertEquals(context, enrichedContext);
        assertEquals(0, fakeService.getCallCount());
    }

    @Test
    void doesNotQueryPrometheusForBlankService() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        IncidentContext context =
                createContext("   ");

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        IncidentContext enrichedContext =
                enricher.enrich(context);

        assertEquals(context, enrichedContext);
        assertEquals(0, fakeService.getCallCount());
    }

    @Test
    void returnsNullForNullContext() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        IncidentContextAnomalyEnricher enricher =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                );

        assertEquals(null, enricher.enrich(null));
        assertEquals(0, fakeService.getCallCount());
    }

    @Test
    void preservesExistingContextFields() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        fakeService.addResult(
                AnomalyMetricType.REQUEST_RATE,
                new AnomalyResult(
                        AnomalyMetricType.REQUEST_RATE,
                        "gateway",
                        100.0,
                        10.0,
                        5.0,
                        18.0,
                        true
                )
        );

        IncidentContext context =
                createContext("gateway");

        IncidentContext enrichedContext =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                ).enrich(context);

        assertEquals(
                context.incident(),
                enrichedContext.incident()
        );

        assertEquals(
                context.severity(),
                enrichedContext.severity()
        );

        assertEquals(
                context.service(),
                enrichedContext.service()
        );

        assertEquals(
                context.alerts(),
                enrichedContext.alerts()
        );

        assertEquals(
                context.metrics(),
                enrichedContext.metrics()
        );

        assertEquals(
                context.logs(),
                enrichedContext.logs()
        );

        assertEquals(
                context.health(),
                enrichedContext.health()
        );

        assertEquals(
                context.httpErrors(),
                enrichedContext.httpErrors()
        );

        assertEquals(
                context.timeline(),
                enrichedContext.timeline()
        );

        assertFalse(enrichedContext.anomalies().isEmpty());
    }

    @Test
    void ignoresMetricWhenDetectionFails() {

        FakePrometheusAnomalyDetectionService fakeService =
                new FakePrometheusAnomalyDetectionService();

        fakeService.setFailure(
                AnomalyMetricType.LATENCY
        );

        fakeService.addResult(
                AnomalyMetricType.REQUEST_RATE,
                new AnomalyResult(
                        AnomalyMetricType.REQUEST_RATE,
                        "order-service",
                        100.0,
                        20.0,
                        10.0,
                        8.0,
                        true
                )
        );

        IncidentContext context =
                createContext("order-service");

        IncidentContext enrichedContext =
                new IncidentContextAnomalyEnricher(
                        fakeService,
                        30,
                        60
                ).enrich(context);

        assertEquals(1, enrichedContext.anomalies().size());

        assertEquals(
                AnomalyMetricType.REQUEST_RATE,
                enrichedContext.anomalies()
                        .get(0)
                        .metricType()
        );
    }

    private AnomalyResult normalResult(
            AnomalyMetricType metricType,
            String service) {

        return new AnomalyResult(
                metricType,
                service,
                10.0,
                10.0,
                2.0,
                0.0,
                false
        );
    }

    private IncidentContext createContext(String service) {

        return new IncidentContext(
                "TestIncident",
                "warning",
                service,
                List.of(
                        new AlertEvidence(
                                "TestAlert",
                                "firing",
                                "warning",
                                service,
                                "localhost",
                                "Test summary",
                                "Test description",
                                "Test runbook",
                                Map.of(),
                                Instant.parse(
                                        "2026-09-24T10:00:00Z"
                                ),
                                null
                        )
                ),
                Map.of(
                        "request_rate",
                        10.0
                ),
                List.of(
                        new LogEvidence(
                                Instant.parse(
                                        "2026-09-24T10:01:00Z"
                                ),
                                "ERROR",
                                service,
                                "request-1",
                                "Test error",
                                "TestException"
                        )
                ),
                new HealthEvidence(
                        "UP",
                        Map.of()
                ),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse(
                                        "2026-09-24T10:02:00Z"
                                ),
                                "GET",
                                "/test",
                                500,
                                service,
                                "request-1",
                                "Test error"
                        )
                ),
                List.of(
                        new TimelineEvent(
                                Instant.parse(
                                        "2026-09-24T10:02:00Z"
                                ),
                                "ALERT",
                                "Test event"
                        )
                )
        );
    }

    private static class FakePrometheusAnomalyDetectionService
            extends PrometheusAnomalyDetectionService {

        private final Map<AnomalyMetricType, AnomalyResult> results =
                new HashMap<>();

        private AnomalyMetricType failureMetricType;

        private int callCount;

        private FakePrometheusAnomalyDetectionService() {
            super(new PrometheusMetricsClient() {

                @Override
                public List<PrometheusMetricSample> queryRange(
                        String query,
                        Instant start,
                        Instant end,
                        long stepSeconds) {

                    return List.of();
                }

                @Override
                public PrometheusMetricSample queryInstant(
                        String query,
                        Instant timestamp) {

                    return null;
                }
            });
        }

        private void addResult(
                AnomalyMetricType metricType,
                AnomalyResult result) {

            results.put(metricType, result);
        }

        private void setFailure(
                AnomalyMetricType metricType) {

            failureMetricType = metricType;
        }

        private int getCallCount() {
            return callCount;
        }

        @Override
        public AnomalyResult detect(
                AnomalyMetricType metricType,
                String service,
                String query,
                Instant end,
                Duration historicalWindow,
                long stepSeconds) {

            callCount++;

            if (metricType == failureMetricType) {
                throw new IllegalStateException(
                        "Simulated Prometheus failure"
                );
            }

            return results.get(metricType);
        }
    }
}
