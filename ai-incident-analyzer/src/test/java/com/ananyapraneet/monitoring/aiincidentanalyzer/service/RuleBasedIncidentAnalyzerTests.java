package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AnalysisSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import org.junit.jupiter.api.Test;

import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.CorrelatedIncidentMapper;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.IncidentContextCorrelationAdapter;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.LogCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.MetricCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.MultiSignalCorrelationEngine;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.ServiceDependencyCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.ServiceDependencyGraph;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.TemporalCorrelator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedIncidentAnalyzerTests {

    private final RuleBasedIncidentAnalyzer analyzer =
            new RuleBasedIncidentAnalyzer(
                    new CorrelationEngine(),
                    new ConfidenceScorer()
            );

    @Test
    void shouldDetectServerSideFailureFromHttp5xxErrors() {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "HIGH",
                "order-service",
                List.of(),
                Map.of("request_rate", 12.5),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-11T10:00:00Z"),
                                "GET",
                                "/orders/123",
                                500,
                                "order-service",
                                "request-123",
                                "Internal Server Error"
                        )
                ),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals("APIErrorRateHigh", analysis.incident());
        assertEquals(AnalysisSeverity.HIGH, analysis.severity());
        assertEquals("order-service", analysis.service());
        assertTrue(analysis.summary().contains("server-side"));
        assertTrue(analysis.probableRootCause().contains("Server-side"));
        assertFalse(analysis.evidence().isEmpty());
        assertTrue(analysis.confidence() > 0.0);
    }

    @Test
    void shouldDetectDegradedServiceHealth() {
        IncidentContext context = new IncidentContext(
                "ServiceHealthDegraded",
                "CRITICAL",
                "user-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence(
                        "DOWN",
                        Map.of("db", Map.of("status", "DOWN"))
                ),
                List.of(),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals(AnalysisSeverity.CRITICAL, analysis.severity());
        assertEquals("user-service", analysis.service());
        assertTrue(analysis.summary().contains("degraded health"));
        assertTrue(analysis.probableRootCause().contains("health degradation"));
        assertTrue(analysis.confidence() > 0.0);
    }

    @Test
    void shouldNotClassifyFourHundredErrorsAsServerFailure() {
        IncidentContext context = new IncidentContext(
                "ClientErrorRateHigh",
                "MEDIUM",
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-11T10:00:00Z"),
                                "GET",
                                "/orders/999",
                                404,
                                "order-service",
                                "request-404",
                                "Order not found"
                        )
                ),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals(AnalysisSeverity.MEDIUM, analysis.severity());
        assertFalse(analysis.summary().contains("server-side HTTP errors"));
        assertFalse(
                analysis.probableRootCause()
                        .contains("Server-side application failure")
        );
        assertFalse(analysis.evidence().isEmpty());
    }

    @Test
    void shouldRemainCautiousWhenNoEvidenceExists() {
        IncidentContext context = new IncidentContext(
                "UnknownIncident",
                null,
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals(AnalysisSeverity.UNKNOWN, analysis.severity());
        assertEquals(
                "Insufficient evidence to determine a probable root cause.",
                analysis.probableRootCause()
        );
        assertEquals(0.10, analysis.confidence());
        assertFalse(analysis.recommendedRemediation().isEmpty());
    }

    @Test
    void shouldRemainCautiousWithDatabaseLikeMetricWithoutDatabaseEvidence() {
        IncidentContext context = new IncidentContext(
                "DatabaseConnectionPressure",
                "HIGH",
                "order-service",
                List.of(),
                Map.of("db_connection_utilization", 96.0),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals(AnalysisSeverity.HIGH, analysis.severity());
        assertTrue(
                analysis.probableRootCause()
                        .contains("No specific root cause")
        );
        assertFalse(
                analysis.probableRootCause()
                        .toLowerCase()
                        .contains("database")
        );
        assertEquals(0.40, analysis.confidence());
    }

    @Test
    void shouldHandleUnknownServiceWithoutInventingDiagnosis() {
        IncidentContext context = new IncidentContext(
                "UnknownServiceIncident",
                "HIGH",
                "payment-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(),
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertEquals("payment-service", analysis.service());
        assertEquals(AnalysisSeverity.HIGH, analysis.severity());
        assertEquals(
                "Insufficient evidence to determine a probable root cause.",
                analysis.probableRootCause()
        );
        assertEquals(0.10, analysis.confidence());
    }

    @Test
    void shouldIgnoreNullAndMissingEvidenceSafely() {
        List<AlertEvidence> alerts = new ArrayList<>();

        alerts.add(
                new AlertEvidence(
                        "TestAlert",
                        "firing",
                        "MEDIUM",
                        "order-service",
                        "instance-1",
                        null,
                        null,
                        null,
                        Map.of(),
			null,
			null
                )
        );

        alerts.add(null);

        List<HttpErrorEvidence> httpErrors = new ArrayList<>();

        httpErrors.add(
                new HttpErrorEvidence(
                        Instant.parse("2026-09-11T10:00:00Z"),
                        "GET",
                        "/orders/1",
                        500,
                        "order-service",
                        "request-500",
                        null
                )
        );

        httpErrors.add(null);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("valid_metric", 10.0);
        metrics.put("missing_metric", null);

        IncidentContext context = new IncidentContext(
                "MalformedEvidenceIncident",
                "MEDIUM",
                "order-service",
                alerts,
                metrics,
                List.of(),
                new HealthEvidence("UP", Map.of()),
                httpErrors,
                List.of()
        );

        IncidentAnalysis analysis = analyzer.analyze(context);

        assertNotNull(analysis);
        assertEquals(AnalysisSeverity.MEDIUM, analysis.severity());
        assertFalse(analysis.evidence().isEmpty());
        assertEquals(0.65, analysis.confidence());
    }

    @Test
    void shouldHandleNullContextSafely() {
        IncidentAnalysis analysis = analyzer.analyze(null);

        assertNotNull(analysis);
        assertEquals("Unknown Incident", analysis.incident());
        assertEquals(AnalysisSeverity.UNKNOWN, analysis.severity());
        assertEquals("unknown", analysis.service());
        assertEquals(0.0, analysis.confidence());
        assertFalse(analysis.recommendedRemediation().isEmpty());
    }

    @Test
    void shouldAddStage13CorrelationEvidence() {

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

        IncidentContext context = new IncidentContext(
                "OrderServiceLatency",
                "HIGH",
                "order-service",
                List.of(alert),
                Map.of(),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(),
                List.of()
        );

        RuleBasedIncidentAnalyzer stage13Analyzer =
                new RuleBasedIncidentAnalyzer(
                        new CorrelationEngine(),
                        new ConfidenceScorer(),
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
			)
                );

        IncidentAnalysis analysis =
                stage13Analyzer.analyze(context);

        assertNotNull(analysis);

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "CORRELATION".equals(evidence.type())
                        )
        );
    }
}
