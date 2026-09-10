package com.ananyapraneet.monitoring.incidentcontext;

import com.ananyapraneet.monitoring.incidentcontext.model.AlertEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.TimelineEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentContextModelTests {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldSerializeIncidentContext() throws Exception {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "HIGH",
                "order-service",
                List.of(
                        new AlertEvidence(
                                "HighHttp5xxErrorRate",
                                "firing",
                                "warning",
                                "order-service",
                                "order-service:8081",
                                "High HTTP 5xx error rate",
                                "HTTP 5xx error rate exceeded threshold",
                                "Check order-service logs and database health",
                                Map.of(
                                        "alertname", "HighHttp5xxErrorRate",
                                        "service", "order-service",
                                        "severity", "warning"
                                )
                        )
                ),
                Map.of(
                        "http_5xx_rate", 0.08,
                        "request_rate", 125.0
                ),
                List.of(
                        new LogEvidence(
                                Instant.parse("2026-09-10T15:00:00Z"),
                                "ERROR",
                                "order-service",
                                "req-123",
                                "Database connection failed",
                                "SQLException"
                        )
                ),
                new HealthEvidence(
                        "DOWN",
                        Map.of(
                                "db", "DOWN",
                                "redis", "UP"
                        )
                ),
                List.of(
                        new HttpErrorEvidence(
                                Instant.parse("2026-09-10T15:00:01Z"),
                                "POST",
                                "/orders",
                                500,
                                "order-service",
                                "req-123",
                                "Database connection failed"
                        )
                ),
                List.of(
                        new TimelineEvent(
                                Instant.parse("2026-09-10T15:00:00Z"),
                                "ALERT_TRIGGERED",
                                "High HTTP 5xx error rate detected"
                        ),
                        new TimelineEvent(
                                Instant.parse("2026-09-10T15:00:01Z"),
                                "HTTP_ERROR",
                                "POST /orders returned HTTP 500"
                        )
                )
        );

        String json = objectMapper.writeValueAsString(context);

        assertTrue(json.contains("\"incident\":\"APIErrorRateHigh\""));
        assertTrue(json.contains("\"severity\":\"HIGH\""));
        assertTrue(json.contains("\"service\":\"order-service\""));
        assertTrue(json.contains("\"alerts\""));
        assertTrue(json.contains("\"metrics\""));
        assertTrue(json.contains("\"logs\""));
        assertTrue(json.contains("\"health\""));
        assertTrue(json.contains("\"httpErrors\""));
        assertTrue(json.contains("\"timeline\""));

        Map<String, Object> jsonMap =
                objectMapper.readValue(json, new TypeReference<>() {});

        assertEquals("APIErrorRateHigh", jsonMap.get("incident"));
        assertEquals("HIGH", jsonMap.get("severity"));
        assertEquals("order-service", jsonMap.get("service"));
    }
}
