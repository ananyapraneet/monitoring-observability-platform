package com.ananyapraneet.monitoring.incidentcontext.service;

import com.ananyapraneet.monitoring.incidentcontext.health.HealthClient;
import com.ananyapraneet.monitoring.incidentcontext.http.HttpErrorClient;
import com.ananyapraneet.monitoring.incidentcontext.log.LogClient;
import com.ananyapraneet.monitoring.incidentcontext.log.StructuredLogClient;
import com.ananyapraneet.monitoring.incidentcontext.metrics.PrometheusClient;
import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.store.InMemoryLogEvidenceStore;
import com.ananyapraneet.monitoring.incidentcontext.store.LogEvidenceStore;
import com.ananyapraneet.monitoring.incidentcontext.timeline.TimelineBuilder;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerWebhookRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentContextBuilderTests {

    private final AlertmanagerAlertNormalizer normalizer =
            new AlertmanagerAlertNormalizer();

    private final PrometheusClient prometheusClient = service -> Map.of(
            "http_5xx_rate", 0.08,
            "request_rate", 125.0
    );

    private final HealthClient healthClient = service -> new HealthEvidence(
            "UP",
            Map.of()
    );

    private final HttpErrorClient httpErrorClient = service -> List.of(
            new HttpErrorEvidence(
                    Instant.parse("2026-09-10T20:00:34.337Z"),
                    "GET",
                    "/orders/{id}",
                    404,
                    service,
                    null,
                    null
            )
    );

    private final LogClient logClient = service -> List.of(
            new LogEvidence(
                    Instant.parse("2026-09-10T20:00:34.337Z"),
                    "INFO",
                    service,
                    "abc-request-id",
                    "HTTP request completed: method=GET endpoint=/orders/999999999 status=404 durationMs=665",
                    null
            )
    );

    private final TimelineBuilder timelineBuilder =
            new TimelineBuilder();

    @Test
    void shouldBuildIncidentContextWithMetricsHealthHttpErrorsAndLogs() {

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "firing",
                Map.of(
                        "alertname", "HighHttp5xxErrorRate",
                        "severity", "warning",
                        "service", "order-service",
                        "environment", "local",
                        "instance", "order-service:8081"
                ),
                Map.of(
                        "summary", "High HTTP 5xx error rate",
                        "description", "HTTP 5xx error rate exceeded 5%",
                        "runbook", "Check order-service logs and database health"
                ),
                Instant.parse("2026-09-10T15:00:00Z"),
                Instant.parse("0001-01-01T00:00:00Z"),
                "http://prometheus:9090/graph",
                "abc123"
        );

        AlertmanagerWebhookRequest request =
                new AlertmanagerWebhookRequest(
                        "default",
                        "firing",
                        "{}:{alertname=\"HighHttp5xxErrorRate\"}",
                        "0",
                        List.of(alert)
                );

        IncidentContextBuilder builder =
                new IncidentContextBuilder(
                        normalizer,
                        prometheusClient,
                        healthClient,
                        httpErrorClient,
                        logClient,
                        timelineBuilder
                );

        IncidentContext context = builder.build(request);

        assertNotNull(context);

        assertEquals(
                "HighHttp5xxErrorRate",
                context.incident()
        );

        assertEquals(
                "warning",
                context.severity()
        );

        assertEquals(
                "order-service",
                context.service()
        );

        assertNotNull(context.alerts());

        assertEquals(
                1,
                context.alerts().size()
        );

        assertEquals(
                "HighHttp5xxErrorRate",
                context.alerts().get(0).alertName()
        );

        assertEquals(
                "firing",
                context.alerts().get(0).status()
        );

        assertEquals(
                0.08,
                context.metrics().get("http_5xx_rate")
        );

        assertEquals(
                125.0,
                context.metrics().get("request_rate")
        );

        assertNotNull(context.health());

        assertEquals(
                "UP",
                context.health().status()
        );

        assertNotNull(context.health().components());

        assertEquals(
                0,
                context.health().components().size()
        );

        assertNotNull(context.httpErrors());

        assertEquals(
                1,
                context.httpErrors().size()
        );

        assertEquals(
                "GET",
                context.httpErrors().get(0).method()
        );

        assertEquals(
                "/orders/{id}",
                context.httpErrors().get(0).endpoint()
        );

        assertEquals(
                404,
                context.httpErrors().get(0).status()
        );

        assertEquals(
                "order-service",
                context.httpErrors().get(0).service()
        );

        assertNull(
                context.httpErrors().get(0).requestId()
        );

        assertNull(
                context.httpErrors().get(0).message()
        );

        assertNotNull(context.logs());

        assertEquals(
                1,
                context.logs().size()
        );

        assertEquals(
                "INFO",
                context.logs().get(0).level()
        );

        assertEquals(
                "order-service",
                context.logs().get(0).service()
        );

        assertEquals(
                "abc-request-id",
                context.logs().get(0).requestId()
        );

        assertEquals(
                "HTTP request completed: method=GET endpoint=/orders/999999999 status=404 durationMs=665",
                context.logs().get(0).message()
        );

        assertNull(
                context.logs().get(0).exception()
        );

        assertNotNull(context.timeline());

        assertEquals(
                3,
                context.timeline().size()
        );

        assertEquals(
                "ALERT_FIRING",
                context.timeline().get(0).type()
        );

        assertEquals(
                "HTTP_ERROR",
                context.timeline().get(1).type()
        );

        assertEquals(
                "LOG_EVENT",
                context.timeline().get(2).type()
        );
    }

    @Test
    void shouldReturnUnknownHealthAndEmptyEvidenceForUnknownService() {

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "firing",
                Map.of(
                        "alertname", "UnknownServiceAlert",
                        "severity", "warning"
                ),
                Map.of(
                        "summary", "Unknown service"
                ),
                Instant.parse("2026-09-10T15:00:00Z"),
                Instant.parse("0001-01-01T00:00:00Z"),
                "http://prometheus:9090/graph",
                "xyz789"
        );

        AlertmanagerWebhookRequest request =
                new AlertmanagerWebhookRequest(
                        "default",
                        "firing",
                        "{}",
                        "0",
                        List.of(alert)
                );

        IncidentContextBuilder builder =
                new IncidentContextBuilder(
                        normalizer,
                        prometheusClient,
                        healthClient,
                        httpErrorClient,
                        logClient,
                        timelineBuilder
                );

        IncidentContext context = builder.build(request);

        assertNotNull(context);

        assertEquals(
                "UnknownServiceAlert",
                context.incident()
        );

        assertEquals(
                "warning",
                context.severity()
        );

        assertEquals(
                "unknown",
                context.service()
        );

        assertNotNull(context.metrics());

        assertEquals(
                0,
                context.metrics().size()
        );

        assertNotNull(context.health());

        assertEquals(
                "UNKNOWN",
                context.health().status()
        );

        assertNotNull(context.health().components());

        assertEquals(
                0,
                context.health().components().size()
        );

        assertNotNull(context.httpErrors());

        assertEquals(
                0,
                context.httpErrors().size()
        );

        assertNotNull(context.logs());

        assertEquals(
                0,
                context.logs().size()
        );
    }

    @Test
    void shouldIncludeIngestedLogsWhenBuildingIncidentContext() {

        LogEvidenceStore logEvidenceStore =
                new InMemoryLogEvidenceStore();

        StructuredLogClient structuredLogClient =
                new StructuredLogClient(logEvidenceStore);

        LogEvidence ingestedLog = new LogEvidence(
                Instant.parse("2026-09-25T12:00:00Z"),
                "ERROR",
                "order-service",
                "stage-15-10-request",
                "Failed to acquire database connection",
                "java.sql.SQLTransientConnectionException"
        );

        logEvidenceStore.add(ingestedLog);

        IncidentContextBuilder builder =
                new IncidentContextBuilder(
                        normalizer,
                        prometheusClient,
                        healthClient,
                        httpErrorClient,
                        structuredLogClient,
                        timelineBuilder
                );

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "firing",
                Map.of(
                        "alertname", "DatabaseConnectionPoolExhausted",
                        "severity", "critical",
                        "service", "order-service",
                        "environment", "local",
                        "instance", "order-service:8081"
                ),
                Map.of(
                        "summary", "Database connection pool exhausted"
                ),
                Instant.parse("2026-09-25T12:00:00Z"),
                Instant.parse("0001-01-01T00:00:00Z"),
                "http://prometheus:9090/graph",
                "stage-15-10-alert"
        );

        AlertmanagerWebhookRequest request =
                new AlertmanagerWebhookRequest(
                        "default",
                        "firing",
                        "{}",
                        "0",
                        List.of(alert)
                );

        IncidentContext context =
                builder.build(request);

        assertNotNull(context);
        assertNotNull(context.logs());

        assertEquals(
                1,
                context.logs().size()
        );

        assertEquals(
                "ERROR",
                context.logs().get(0).level()
        );

        assertEquals(
                "order-service",
                context.logs().get(0).service()
        );

        assertEquals(
                "stage-15-10-request",
                context.logs().get(0).requestId()
        );

        assertEquals(
                "Failed to acquire database connection",
                context.logs().get(0).message()
        );

        assertEquals(
                "java.sql.SQLTransientConnectionException",
                context.logs().get(0).exception()
        );

        assertTrue(
                context.timeline().stream()
                        .anyMatch(event ->
                                "LOG_EVENT".equals(event.type())
                        )
        );
    }
}
