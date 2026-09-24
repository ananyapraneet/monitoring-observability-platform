package com.ananyapraneet.monitoring.incidentcontext;

import com.ananyapraneet.monitoring.incidentcontext.model.AlertEvidence;
import com.ananyapraneet.monitoring.incidentcontext.service.AlertmanagerAlertNormalizer;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertmanagerAlertNormalizerTests {

    private final AlertmanagerAlertNormalizer normalizer =
            new AlertmanagerAlertNormalizer();

    @Test
    void shouldNormalizeAlertmanagerAlert() {
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

        AlertEvidence evidence = normalizer.normalize(alert);

        assertEquals("HighHttp5xxErrorRate", evidence.alertName());
        assertEquals("firing", evidence.status());
        assertEquals("warning", evidence.severity());
        assertEquals("order-service", evidence.service());
        assertEquals("order-service:8081", evidence.instance());
        assertEquals("High HTTP 5xx error rate", evidence.summary());
        assertEquals("HTTP 5xx error rate exceeded 5%", evidence.description());
        assertEquals(
                "Check order-service logs and database health",
                evidence.runbook()
        );
        assertEquals("local", evidence.labels().get("environment"));
        assertEquals(
        	Instant.parse("2026-09-10T15:00:00Z"),
        	evidence.startsAt()
	);

	assertEquals(
        	Instant.parse("0001-01-01T00:00:00Z"),
        	evidence.endsAt()
	);
    }
}
