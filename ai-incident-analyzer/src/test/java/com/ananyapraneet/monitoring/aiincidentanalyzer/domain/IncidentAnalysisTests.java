package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentAnalysisTests {

    @Test
    void shouldCreateIncidentAnalysis() {
        Correlation correlation = new Correlation(
                "HTTP errors increased alongside elevated request latency.",
                List.of("http_5xx_rate", "request_rate", "HTTP_ERROR")
        );

        List<Evidence> evidence = List.of(
                new Evidence(
                        "METRIC",
                        "Prometheus",
                        "5xx error rate increased significantly."
                ),
                new Evidence(
                        "HTTP_ERROR",
                        "incident-context",
                        "Order service returned HTTP 500 errors."
                )
        );

        IncidentAnalysis analysis = new IncidentAnalysis(
                "API Degradation",
                AnalysisSeverity.HIGH,
                "order-service",
                "The order service is experiencing elevated API errors.",
                correlation,
                evidence,
                "Insufficient evidence to determine a definitive root cause.",
                List.of(
                        "Inspect recent order-service deployments.",
                        "Review database connection and timeout metrics."
                ),
                0.75
        );

        assertEquals("API Degradation", analysis.incident());
        assertEquals(AnalysisSeverity.HIGH, analysis.severity());
        assertEquals("order-service", analysis.service());
        assertEquals(2, analysis.evidence().size());
        assertEquals(0.75, analysis.confidence());
    }

    @Test
    void shouldSerializeIncidentAnalysis() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        IncidentAnalysis analysis = new IncidentAnalysis(
                "API Degradation",
                AnalysisSeverity.HIGH,
                "order-service",
                "Elevated API errors detected.",
                new Correlation(
                        "Errors correlate with elevated request latency.",
                        List.of("http_5xx_rate", "request_rate")
                ),
                List.of(
                        new Evidence(
                                "METRIC",
                                "Prometheus",
                                "5xx rate increased."
                        )
                ),
                "Insufficient evidence.",
                List.of(
                        "Inspect service health.",
                        "Review recent deployments."
                ),
                0.70
        );

        String json = objectMapper.writeValueAsString(analysis);

        assertTrue(json.contains("\"incident\":\"API Degradation\""));
        assertTrue(json.contains("\"severity\":\"HIGH\""));
        assertTrue(json.contains("\"service\":\"order-service\""));
        assertTrue(json.contains("\"evidence\""));
        assertTrue(json.contains("\"probableRootCause\""));
        assertTrue(json.contains("\"recommendedRemediation\""));
        assertTrue(json.contains("\"confidence\":0.7"));
    }
}
