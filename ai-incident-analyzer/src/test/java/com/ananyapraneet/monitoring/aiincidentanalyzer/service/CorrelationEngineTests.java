package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorrelationEngineTests {

    private final CorrelationEngine engine = new CorrelationEngine();

    @Test
    void shouldCorrelateServerErrorsWithDegradedHealth() {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                new HealthEvidence("DOWN", Map.of()),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-11T10:00:00Z"),
                                "GET",
                                "/orders/123",
                                500,
                                "order-service",
                                "request-500",
                                "Internal Server Error"
                        )
                ),
                List.of()
        );

        Correlation correlation = engine.correlate(context);

        assertEquals(
                "Server-side HTTP errors and degraded service health are correlated.",
                correlation.description()
        );
        assertTrue(correlation.relatedSignals().contains("http_errors"));
        assertTrue(correlation.relatedSignals().contains("degraded_health"));
    }

    @Test
    void shouldCorrelateServerErrorsWithMetrics() {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "HIGH",
                "order-service",
                List.of(),
                Map.of("http_5xx_rate", 2.5),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-11T10:00:00Z"),
                                "GET",
                                "/orders/123",
                                503,
                                "order-service",
                                "request-503",
                                "Service Unavailable"
                        )
                ),
                List.of()
        );

        Correlation correlation = engine.correlate(context);

        assertEquals(
                "Server-side HTTP errors and metric signals indicate correlated API degradation.",
                correlation.description()
        );
        assertTrue(correlation.relatedSignals().contains("metrics"));
        assertTrue(correlation.relatedSignals().contains("http_errors"));
    }

    @Test
    void shouldNotTreatClientErrorsAsServerFailure() {
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

        Correlation correlation = engine.correlate(context);

        assertEquals(
                "Client-side HTTP errors indicate request or resource access failures.",
                correlation.description()
        );
        assertTrue(correlation.relatedSignals().contains("http_errors"));
    }

    @Test
    void shouldHandleMissingSignalsSafely() {
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

        Correlation correlation = engine.correlate(context);

        assertEquals(
                "No meaningful signals were available for correlation.",
                correlation.description()
        );
        assertTrue(correlation.relatedSignals().isEmpty());
    }

    @Test
    void shouldPrioritizeServerErrorsAndDegradedHealthOverOtherSignals() {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "CRITICAL",
                "order-service",
                List.of(),
                Map.of("http_5xx_rate", 8.7),
                List.of(),
                new HealthEvidence("DOWN", Map.of()),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-11T10:00:00Z"),
                                "GET",
                                "/orders/123",
                                500,
                                "order-service",
                                "request-500",
                                "Internal Server Error"
                        )
                ),
                List.of()
        );

        Correlation correlation = engine.correlate(context);

        assertEquals(
                "Server-side HTTP errors and degraded service health are correlated.",
                correlation.description()
        );

        assertEquals(
                List.of("metrics", "http_errors", "degraded_health"),
                correlation.relatedSignals()
        );
    }

    @Test
    void shouldHandleNullContextSafely() {
        Correlation correlation = engine.correlate(null);

        assertEquals(
                "No signals were available for correlation.",
                correlation.description()
        );
        assertTrue(correlation.relatedSignals().isEmpty());
    }
}
