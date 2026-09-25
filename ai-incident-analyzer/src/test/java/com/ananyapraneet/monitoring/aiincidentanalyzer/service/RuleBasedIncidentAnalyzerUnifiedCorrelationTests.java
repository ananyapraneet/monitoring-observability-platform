package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.TimelineEvent;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.CorrelatedIncidentMapper;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.IncidentContextCorrelationAdapter;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.LogCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.MetricCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.MultiSignalCorrelationEngine;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.ServiceDependencyCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.ServiceDependencyGraph;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.TemporalCorrelator;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedIncidentAnalyzerUnifiedCorrelationTests {

    @Test
    void addsUnifiedEvidenceUsingStage13CorrelationAndAnomaly() {
        IncidentContextCorrelationAdapter correlationAdapter =
                createCorrelationAdapter();

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(correlationAdapter);

        Instant firstAlertTime =
                Instant.parse("2026-09-24T10:00:00Z");

        Instant secondAlertTime =
                Instant.parse("2026-09-24T10:02:00Z");

        AlertEvidence postgresAlert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "critical",
                        "postgresql",
                        "postgres-1",
                        firstAlertTime,
                        "PostgreSQL CPU usage is high."
                );

        AlertEvidence orderServiceAlert =
                createAlert(
                        "OrderServiceHighLatency",
                        "warning",
                        "order-service",
                        "order-service-1",
                        secondAlertTime,
                        "Order service latency is high."
                );

        AnomalyEvidence latencyAnomaly =
                createLatencyAnomaly(secondAlertTime);

        LogEvidence databaseLog =
                new LogEvidence(
                        secondAlertTime,
                        "ERROR",
                        "order-service",
                        "corr-123",
                        "Database connection failure while processing order.",
                        "PSQLException"
                );

        IncidentContext context =
                createContext(
                        List.of(
                                postgresAlert,
                                orderServiceAlert
                        ),
                        List.of(databaseLog),
                        List.of(latencyAnomaly)
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        List<Evidence> evidence =
                analysis.evidence();

        assertContainsEvidenceType(evidence, "ALERT");
        assertContainsEvidenceType(evidence, "ANOMALY");
        assertContainsEvidenceType(evidence, "METRIC");
        assertContainsEvidenceType(evidence, "CORRELATION");
        assertContainsEvidenceType(
                evidence,
                "UNIFIED_CORRELATION"
        );

        Evidence unifiedEvidence =
                findEvidence(
                        evidence,
                        "UNIFIED_CORRELATION"
                );

        assertNotNull(unifiedEvidence);

        assertTrue(
                unifiedEvidence.description()
                        .contains("Anomalous metric behavior")
        );

        assertTrue(
                unifiedEvidence.description()
                        .contains("Stage 13 correlation evidence")
        );

        assertTrue(
                unifiedEvidence.description()
                        .contains("does not by itself establish causation")
        );

        assertTrue(
                unifiedEvidence.description()
                        .contains("definitive root cause")
        );
    }

    @Test
    void preservesAnomalyEvidenceWithoutTreatingItAsRootCause() {
        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(null);

        Instant timestamp =
                Instant.parse("2026-09-24T10:02:00Z");

        AnomalyEvidence anomaly =
                createLatencyAnomaly(timestamp);

        IncidentContext context =
                createContext(
                        List.of(),
                        List.of(),
                        List.of(anomaly)
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        Evidence anomalyEvidence =
                findEvidence(
                        analysis.evidence(),
                        "ANOMALY"
                );

        assertNotNull(anomalyEvidence);

        assertTrue(
                anomalyEvidence.description()
                        .contains("LATENCY")
        );

        assertTrue(
                anomalyEvidence.description()
                        .contains("order-service")
        );

        assertTrue(
                anomalyEvidence.description()
                        .contains("not proof of root cause")
        );

        assertTrue(
                analysis.probableRootCause()
                        .contains(
                                "anomaly alone does not establish the root cause"
                        )
        );
    }

    @Test
    void doesNotCreateUnifiedCorrelationForAnomalyAlone() {
        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(null);

        Instant timestamp =
                Instant.parse("2026-09-24T10:02:00Z");

        AnomalyEvidence anomaly =
                createLatencyAnomaly(timestamp);

        IncidentContext context =
                createContext(
                        List.of(),
                        List.of(),
                        List.of(anomaly)
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertContainsEvidenceType(
                analysis.evidence(),
                "ANOMALY"
        );

        assertFalse(
                containsEvidenceType(
                        analysis.evidence(),
                        "UNIFIED_CORRELATION"
                )
        );

        assertTrue(
                analysis.probableRootCause()
                        .contains(
                                "anomaly alone does not establish the root cause"
                        )
        );
    }

    @Test
    void keepsAlertAndAnomalyEvidenceSeparateFromUnifiedCorrelation() {
        IncidentContextCorrelationAdapter correlationAdapter =
                createCorrelationAdapter();

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(correlationAdapter);

        Instant timestamp =
                Instant.parse("2026-09-24T10:02:00Z");

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "warning",
                        "order-service",
                        "order-service-1",
                        timestamp,
                        "Order service latency is high."
                );

        AnomalyEvidence anomaly =
                createLatencyAnomaly(timestamp);

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "corr-456",
                        "Database connection failure.",
                        "PSQLException"
                );

        IncidentContext context =
                createContext(
                        List.of(alert),
                        List.of(log),
                        List.of(anomaly)
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertContainsEvidenceType(
                analysis.evidence(),
                "ALERT"
        );

        assertContainsEvidenceType(
                analysis.evidence(),
                "ANOMALY"
        );

        assertContainsEvidenceType(
                analysis.evidence(),
                "UNIFIED_CORRELATION"
        );

        Evidence anomalyEvidence =
                findEvidence(
                        analysis.evidence(),
                        "ANOMALY"
                );

        Evidence unifiedEvidence =
                findEvidence(
                        analysis.evidence(),
                        "UNIFIED_CORRELATION"
                );

        assertNotNull(anomalyEvidence);
        assertNotNull(unifiedEvidence);

        assertTrue(
                anomalyEvidence.description()
                        .contains("not proof of root cause")
        );

        assertTrue(
                unifiedEvidence.description()
                        .contains(
                                "does not by itself establish causation"
                        )
        );
    }

    @Test
    void supportsMultipleAnomaliesAlongsideCorrelationEvidence() {
        IncidentContextCorrelationAdapter correlationAdapter =
                createCorrelationAdapter();

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(correlationAdapter);

        Instant timestamp =
                Instant.parse("2026-09-24T10:02:00Z");

        AlertEvidence alert =
                createAlert(
                        "OrderServiceHighLatency",
                        "warning",
                        "order-service",
                        "order-service-1",
                        timestamp,
                        "Order service latency is high."
                );

        AnomalyEvidence latencyAnomaly =
                new AnomalyEvidence(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        2.4,
                        0.35,
                        0.30,
                        6.83,
                        true,
                        timestamp
                );

        AnomalyEvidence errorRateAnomaly =
                new AnomalyEvidence(
                        AnomalyMetricType.ERROR_RATE,
                        "order-service",
                        0.25,
                        0.02,
                        0.01,
                        23.0,
                        true,
                        timestamp
                );

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "corr-789",
                        "Database connection failure.",
                        "PSQLException"
                );

        IncidentContext context =
                createContext(
                        List.of(alert),
                        List.of(log),
                        List.of(
                                latencyAnomaly,
                                errorRateAnomaly
                        )
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertEquals(
                2,
                countEvidenceType(
                        analysis.evidence(),
                        "ANOMALY"
                )
        );

        assertContainsEvidenceType(
                analysis.evidence(),
                "UNIFIED_CORRELATION"
        );
    }

    @Test
    void doesNotClaimCausationWhenMultipleSignalsAreCorrelated() {
        IncidentContextCorrelationAdapter correlationAdapter =
                createCorrelationAdapter();

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer(correlationAdapter);

        Instant firstAlertTime =
                Instant.parse("2026-09-24T10:00:00Z");

        Instant secondAlertTime =
                Instant.parse("2026-09-24T10:02:00Z");

        AlertEvidence postgresAlert =
                createAlert(
                        "PostgreSQLHighCPU",
                        "critical",
                        "postgresql",
                        "postgres-1",
                        firstAlertTime,
                        "PostgreSQL CPU usage is high."
                );

        AlertEvidence orderServiceAlert =
                createAlert(
                        "OrderServiceHighLatency",
                        "warning",
                        "order-service",
                        "order-service-1",
                        secondAlertTime,
                        "Order service latency is high."
                );

        AnomalyEvidence anomaly =
                createLatencyAnomaly(secondAlertTime);

        LogEvidence log =
                new LogEvidence(
                        secondAlertTime,
                        "ERROR",
                        "order-service",
                        "corr-999",
                        "Database connection failure while processing order.",
                        "PSQLException"
                );

        IncidentContext context =
                createContext(
                        List.of(
                                postgresAlert,
                                orderServiceAlert
                        ),
                        List.of(log),
                        List.of(anomaly)
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        Evidence unifiedEvidence =
                findEvidence(
                        analysis.evidence(),
                        "UNIFIED_CORRELATION"
                );

        assertNotNull(unifiedEvidence);

        String description =
                unifiedEvidence.description()
                        .toLowerCase();

        assertTrue(
                description.contains("correlation")
        );

        assertTrue(
                description.contains("does not by itself establish causation")
        );

        assertTrue(
                description.contains("definitive root cause")
        );

        assertTrue(
                analysis.probableRootCause()
                        .contains("specific root cause")
                        || analysis.probableRootCause()
                        .contains("root cause")
        );
    }

    private IncidentContextCorrelationAdapter createCorrelationAdapter() {
        return new IncidentContextCorrelationAdapter(
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
    }

    private RuleBasedIncidentAnalyzer createAnalyzer(
            IncidentContextCorrelationAdapter correlationAdapter) {

        if (correlationAdapter == null) {
            return new RuleBasedIncidentAnalyzer(
                    new CorrelationEngine(),
                    new ConfidenceScorer()
            );
        }

        return new RuleBasedIncidentAnalyzer(
                new CorrelationEngine(),
                new ConfidenceScorer(),
                correlationAdapter
        );
    }

    private AnomalyEvidence createLatencyAnomaly(
            Instant timestamp) {

        return new AnomalyEvidence(
                AnomalyMetricType.LATENCY,
                "order-service",
                2.4,
                0.35,
                0.30,
                6.83,
                true,
                timestamp
        );
    }

    private AlertEvidence createAlert(
            String alertName,
            String severity,
            String service,
            String instance,
            Instant timestamp,
            String summary) {

        return new AlertEvidence(
                alertName,
                "firing",
                severity,
                service,
                instance,
                summary,
                summary,
                "Investigate the affected service.",
                Map.of(),
                timestamp,
                timestamp.plusSeconds(300)
        );
    }

    private IncidentContext createContext(
            List<AlertEvidence> alerts,
            List<LogEvidence> logs,
            List<AnomalyEvidence> anomalies) {

        return new IncidentContext(
                "OrderServiceIncident",
                "critical",
                "order-service",
                alerts,
                Map.of(
                        "request_rate",
                        12.5
                ),
                logs,
                new HealthEvidence(
                        "UP",
                        Map.of()
                ),
                List.<HttpErrorEvidence>of(),
                List.<TimelineEvent>of(),
                anomalies
        );
    }

    private Evidence findEvidence(
            List<Evidence> evidence,
            String type) {

        for (Evidence item : evidence) {
            if (item != null
                    && type.equals(item.type())) {
                return item;
            }
        }

        return null;
    }

    private boolean containsEvidenceType(
            List<Evidence> evidence,
            String type) {

        return findEvidence(evidence, type) != null;
    }

    private long countEvidenceType(
            List<Evidence> evidence,
            String type) {

        long count = 0;

        for (Evidence item : evidence) {
            if (item != null
                    && type.equals(item.type())) {
                count++;
            }
        }

        return count;
    }

    private void assertContainsEvidenceType(
            List<Evidence> evidence,
            String type) {

        assertTrue(
                containsEvidenceType(evidence, type),
                "Expected evidence type: " + type
        );
    }
}
