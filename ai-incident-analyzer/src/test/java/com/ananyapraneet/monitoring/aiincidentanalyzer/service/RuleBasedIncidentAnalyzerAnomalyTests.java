package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.IncidentContextCorrelationAdapter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedIncidentAnalyzerAnomalyTests {

    @Test
    void addsAnomalyEvidenceToAnalysis() {

        IncidentContext context =
                createContext(
                        List.of(
                                new AnomalyEvidence(
                                        AnomalyMetricType.LATENCY,
                                        "order-service",
                                        2.8,
                                        0.45,
                                        0.55,
                                        4.27,
                                        true,
                                        Instant.now()
                                )
                        )
                );

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer();

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
                        .contains("4.27")
        );

        assertTrue(
                anomalyEvidence.description()
                        .contains("not proof of root cause")
        );
    }

    @Test
    void supportsMultipleAnomalies() {

        IncidentContext context =
                createContext(
                        List.of(
                                new AnomalyEvidence(
                                        AnomalyMetricType.LATENCY,
                                        "order-service",
                                        2.8,
                                        0.45,
                                        0.55,
                                        4.27,
                                        true,
                                        Instant.now()
                                ),
                                new AnomalyEvidence(
                                        AnomalyMetricType.ERROR_RATE,
                                        "order-service",
                                        0.15,
                                        0.02,
                                        0.03,
                                        4.33,
                                        true,
                                        Instant.now()
                                )
                        )
                );

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        long anomalyEvidenceCount =
                countEvidenceType(
                        analysis.evidence(),
                        "ANOMALY"
                );

        assertEquals(2, anomalyEvidenceCount);
    }

    @Test
    void handlesEmptyAnomalyList() {

        IncidentContext context =
                createContext(List.of());

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertEquals(
                0,
                countEvidenceType(
                        analysis.evidence(),
                        "ANOMALY"
                )
        );
    }

    @Test
    void handlesNullAnomalyList() {

        IncidentContext context =
                createContext(null);

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertEquals(
                0,
                countEvidenceType(
                        analysis.evidence(),
                        "ANOMALY"
                )
        );
    }

    @Test
    void preservesExistingAlertEvidenceAlongsideAnomalyEvidence() {

        IncidentContext context =
                new IncidentContext(
                        "HighLatency",
                        "warning",
                        "order-service",
                        List.of(
                                new AlertEvidence(
                                        "HighLatency",
                                        "firing",
                                        "Order service latency is high.",
                                        "warning",
                                        "order-service",
                                        "order-service",
                                        "HighLatency",
                                        "http",
                                        Map.of(),
                                        Instant.now(),
                                        Instant.now()
                                )
                        ),
                        Map.of(),
                        List.of(),
                        new HealthEvidence(
                                "UP",
                                Map.of()
                        ),
                        List.of(),
                        List.of(),
                        List.of(
                                new AnomalyEvidence(
                                        AnomalyMetricType.LATENCY,
                                        "order-service",
                                        2.8,
                                        0.45,
                                        0.55,
                                        4.27,
                                        true,
                                        Instant.now()
                                )
                        )
                );

        RuleBasedIncidentAnalyzer analyzer =
                createAnalyzer();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertTrue(
                countEvidenceType(
                        analysis.evidence(),
                        "ALERT"
                ) > 0
        );

        assertTrue(
                countEvidenceType(
                        analysis.evidence(),
                        "ANOMALY"
                ) > 0
        );
    }

    private IncidentContext createContext(
            List<AnomalyEvidence> anomalies) {

        return new IncidentContext(
                "TestIncident",
                "warning",
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence(
                        "UP",
                        Map.of()
                ),
                List.of(),
                List.of(),
                anomalies
        );
    }

    private RuleBasedIncidentAnalyzer createAnalyzer() {

        CorrelationEngine correlationEngine =
                new CorrelationEngine();

        ConfidenceScorer confidenceScorer =
                new ConfidenceScorer();

        IncidentContextCorrelationAdapter
                correlationAdapter = null;

        return new RuleBasedIncidentAnalyzer(
                correlationEngine,
                confidenceScorer,
                correlationAdapter
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
}
