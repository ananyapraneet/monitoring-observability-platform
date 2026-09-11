package com.ananyapraneet.monitoring.incidentcontext.timeline;

import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.TimelineEvent;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimelineBuilderTests {

    private final TimelineBuilder builder =
            new TimelineBuilder();

    @Test
    void shouldBuildChronologicalTimelineFromAlertsHttpErrorsAndLogs() {

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "firing",
                Map.of(
                        "alertname", "HighHttp5xxErrorRate"
                ),
                Map.of(),
                Instant.parse("2026-09-10T15:00:00Z"),
                Instant.parse("0001-01-01T00:00:00Z"),
                "http://prometheus:9090/graph",
                "abc123"
        );

        List<HttpErrorEvidence> httpErrors = List.of(
                new HttpErrorEvidence(
                        Instant.parse("2026-09-10T15:02:00Z"),
                        "GET",
                        "/orders/{id}",
                        404,
                        "order-service",
                        "request-123",
                        "Order not found"
                )
        );

        List<LogEvidence> logs = List.of(
                new LogEvidence(
                        Instant.parse("2026-09-10T15:01:00Z"),
                        "INFO",
                        "order-service",
                        "request-123",
                        "HTTP request completed",
                        null
                )
        );

        List<TimelineEvent> timeline =
                builder.build(
                        List.of(alert),
                        httpErrors,
                        logs
                );

        assertEquals(3, timeline.size());

        assertEquals(
                Instant.parse("2026-09-10T15:00:00Z"),
                timeline.get(0).timestamp()
        );

        assertEquals(
                "ALERT_FIRING",
                timeline.get(0).type()
        );

        assertEquals(
                Instant.parse("2026-09-10T15:01:00Z"),
                timeline.get(1).timestamp()
        );

        assertEquals(
                "LOG_EVENT",
                timeline.get(1).type()
        );

        assertEquals(
                Instant.parse("2026-09-10T15:02:00Z"),
                timeline.get(2).timestamp()
        );

        assertEquals(
                "HTTP_ERROR",
                timeline.get(2).type()
        );
    }

    @Test
    void shouldAddResolvedEventForResolvedAlert() {

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "resolved",
                Map.of(
                        "alertname", "HighHttp5xxErrorRate"
                ),
                Map.of(),
                Instant.parse("2026-09-10T15:00:00Z"),
                Instant.parse("2026-09-10T15:10:00Z"),
                "http://prometheus:9090/graph",
                "abc123"
        );

        List<TimelineEvent> timeline =
                builder.build(
                        List.of(alert),
                        List.of(),
                        List.of()
                );

        assertEquals(2, timeline.size());

        assertEquals(
                "ALERT_FIRING",
                timeline.get(0).type()
        );

        assertEquals(
                "ALERT_RESOLVED",
                timeline.get(1).type()
        );
    }

    @Test
    void shouldIgnoreInvalidAlertEndTimestamp() {

        AlertmanagerAlert alert = new AlertmanagerAlert(
                "firing",
                Map.of(
                        "alertname", "HighHttp5xxErrorRate"
                ),
                Map.of(),
                Instant.parse("2026-09-10T15:00:00Z"),
                Instant.parse("0001-01-01T00:00:00Z"),
                "http://prometheus:9090/graph",
                "abc123"
        );

        List<TimelineEvent> timeline =
                builder.build(
                        List.of(alert),
                        List.of(),
                        List.of()
                );

        assertEquals(1, timeline.size());

        assertEquals(
                "ALERT_FIRING",
                timeline.get(0).type()
        );
    }

    @Test
    void shouldIgnoreEvidenceWithoutTimestamp() {

        List<HttpErrorEvidence> httpErrors = List.of(
                new HttpErrorEvidence(
                        null,
                        "GET",
                        "/orders/{id}",
                        404,
                        "order-service",
                        null,
                        null
                )
        );

        List<LogEvidence> logs = List.of(
                new LogEvidence(
                        null,
                        "INFO",
                        "order-service",
                        null,
                        "Missing timestamp",
                        null
                )
        );

        List<TimelineEvent> timeline =
                builder.build(
                        List.of(),
                        httpErrors,
                        logs
                );

        assertEquals(0, timeline.size());
    }
}
