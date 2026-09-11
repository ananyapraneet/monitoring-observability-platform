package com.ananyapraneet.monitoring.incidentcontext.service;

import com.ananyapraneet.monitoring.incidentcontext.health.HealthClient;
import com.ananyapraneet.monitoring.incidentcontext.http.HttpErrorClient;
import com.ananyapraneet.monitoring.incidentcontext.log.LogClient;
import com.ananyapraneet.monitoring.incidentcontext.metrics.PrometheusClient;
import com.ananyapraneet.monitoring.incidentcontext.model.AlertEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.TimelineEvent;
import com.ananyapraneet.monitoring.incidentcontext.timeline.TimelineBuilder;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerWebhookRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class IncidentContextBuilder {

    private final AlertmanagerAlertNormalizer normalizer;

    private final PrometheusClient prometheusClient;

    private final HealthClient healthClient;

    private final HttpErrorClient httpErrorClient;

    private final LogClient logClient;

    private final TimelineBuilder timelineBuilder;

    public IncidentContextBuilder(
            AlertmanagerAlertNormalizer normalizer,
            PrometheusClient prometheusClient,
            HealthClient healthClient,
            HttpErrorClient httpErrorClient,
            LogClient logClient,
            TimelineBuilder timelineBuilder) {

        this.normalizer = normalizer;
        this.prometheusClient = prometheusClient;
        this.healthClient = healthClient;
        this.httpErrorClient = httpErrorClient;
        this.logClient = logClient;
        this.timelineBuilder = timelineBuilder;
    }

    public IncidentContext build(AlertmanagerWebhookRequest request) {

        List<AlertmanagerAlert> rawAlerts = request.alerts() == null
                ? List.of()
                : request.alerts();

        List<AlertEvidence> alerts = rawAlerts.stream()
                .map(normalizer::normalize)
                .toList();

        AlertEvidence primaryAlert = alerts.isEmpty()
                ? null
                : alerts.get(0);

        String incident = primaryAlert == null
                ? "UnknownIncident"
                : primaryAlert.alertName();

        String severity = primaryAlert == null
                ? "unknown"
                : primaryAlert.severity();

        String service = primaryAlert == null || primaryAlert.service() == null
                ? "unknown"
                : primaryAlert.service();

        Map<String, Object> metrics = "unknown".equals(service)
                ? Collections.emptyMap()
                : prometheusClient.queryMetrics(service);

        HealthEvidence health = "unknown".equals(service)
                ? new HealthEvidence(
                        "UNKNOWN",
                        Collections.emptyMap()
                )
                : healthClient.getHealth(service);

        List<HttpErrorEvidence> httpErrors = "unknown".equals(service)
                ? List.of()
                : httpErrorClient.getHttpErrors(service);

        List<LogEvidence> logs = "unknown".equals(service)
                ? List.of()
                : logClient.getLogs(service);

        List<TimelineEvent> timeline = timelineBuilder.build(
                rawAlerts,
                httpErrors,
                logs
        );

        return new IncidentContext(
                incident,
                severity,
                service,
                alerts,
                metrics,
                logs,
                health,
                httpErrors,
                timeline
        );
    }
}
