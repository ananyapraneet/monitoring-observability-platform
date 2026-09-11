package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AnalysisSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RuleBasedIncidentAnalyzer implements IncidentAnalyzer {

    private final CorrelationEngine correlationEngine;
    private final ConfidenceScorer confidenceScorer;

    public RuleBasedIncidentAnalyzer(
            CorrelationEngine correlationEngine,
            ConfidenceScorer confidenceScorer) {
        this.correlationEngine = correlationEngine;
        this.confidenceScorer = confidenceScorer;
    }

    @Override
    public IncidentAnalysis analyze(IncidentContext context) {

        if (context == null) {
            return new IncidentAnalysis(
                    "Unknown Incident",
                    AnalysisSeverity.UNKNOWN,
                    "unknown",
                    "No incident context was available for analysis.",
                    correlationEngine.correlate(null),
                    List.of(),
                    "Insufficient evidence to determine a probable root cause.",
                    List.of("Collect incident context and retry the analysis."),
                    0.0
            );
        }

        List<Evidence> evidence = new ArrayList<>();
        List<String> remediation = new ArrayList<>();

        addAlertEvidence(
                context.alerts(),
                evidence
        );

        addMetricEvidence(
                context.metrics(),
                evidence
        );

        addHttpErrorEvidence(
                context.httpErrors(),
                evidence
        );

        boolean serviceDegraded = isServiceDegraded(context.health());

        if (serviceDegraded) {
            evidence.add(new Evidence(
                    "HEALTH",
                    "incident-context",
                    "Service health is reported as degraded."
            ));

            remediation.add(
                    "Inspect the service health endpoint and failing health components."
            );
        }

        boolean hasServerErrors = hasServerErrors(context.httpErrors());

        if (hasServerErrors) {
            remediation.add(
                    "Inspect application errors and recent deployments for the affected service."
            );
        }

        if (hasServerErrors && serviceDegraded) {
            remediation.add(
                    "Check downstream dependencies and resource availability."
            );
        }

        AnalysisSeverity severity = determineSeverity(context.severity());

        String rootCause = determineRootCause(
                hasServerErrors,
                serviceDegraded,
                context.metrics(),
                context.logs()
        );

        double confidence = confidenceScorer.score(
                hasServerErrors,
                serviceDegraded,
                context.metrics(),
                context.logs()
        );

        String summary = buildSummary(
                context,
                hasServerErrors,
                serviceDegraded
        );

        Correlation correlation = correlationEngine.correlate(context);

        if (remediation.isEmpty()) {
            remediation.add(
                    "Collect additional metrics, logs, and health evidence before taking corrective action."
            );
        }

        return new IncidentAnalysis(
                context.incident(),
                severity,
                context.service(),
                summary,
                correlation,
                evidence,
                rootCause,
                remediation,
                confidence
        );
    }

    private void addAlertEvidence(
            List<AlertEvidence> alerts,
            List<Evidence> evidence) {

        if (alerts == null) {
            return;
        }

        for (AlertEvidence alert : alerts) {
            if (alert == null) {
                continue;
            }

            String description = alert.summary();

            if (description == null || description.isBlank()) {
                description =
                        "Alert " + alert.alertName() + " is " + alert.status() + ".";
            }

            evidence.add(new Evidence(
                    "ALERT",
                    "alertmanager",
                    description
            ));
        }
    }

    private void addMetricEvidence(
            Map<String, Object> metrics,
            List<Evidence> evidence) {

        if (metrics == null || metrics.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            evidence.add(new Evidence(
                    "METRIC",
                    "prometheus",
                    entry.getKey() + " = " + entry.getValue()
            ));
        }
    }

    private void addHttpErrorEvidence(
            List<HttpErrorEvidence> errors,
            List<Evidence> evidence) {

        if (errors == null) {
            return;
        }

        for (HttpErrorEvidence error : errors) {
            if (error == null) {
                continue;
            }

            evidence.add(new Evidence(
                    "HTTP_ERROR",
                    "incident-context",
                    error.method() + " "
                            + error.endpoint()
                            + " returned HTTP "
                            + error.status()
            ));
        }
    }

    private boolean hasServerErrors(List<HttpErrorEvidence> errors) {

        if (errors == null) {
            return false;
        }

        for (HttpErrorEvidence error : errors) {
            if (error != null
                    && error.status() >= 500
                    && error.status() <= 599) {
                return true;
            }
        }

        return false;
    }

    private boolean isServiceDegraded(HealthEvidence health) {

        if (health == null || health.status() == null) {
            return false;
        }

        return !health.status().equalsIgnoreCase("UP");
    }

    private AnalysisSeverity determineSeverity(String severity) {

        if (severity == null || severity.isBlank()) {
            return AnalysisSeverity.UNKNOWN;
        }

        try {
            return AnalysisSeverity.valueOf(
                    severity.toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            return AnalysisSeverity.UNKNOWN;
        }
    }

    private String determineRootCause(
            boolean hasServerErrors,
            boolean serviceDegraded,
            Map<String, Object> metrics,
            List<?> logs) {

        if (hasServerErrors && serviceDegraded) {
            return "Application or dependency failure is suspected, but the available evidence is insufficient to identify a specific root cause.";
        }

        if (hasServerErrors) {
            return "Server-side application failure is indicated by HTTP 5xx errors; the specific root cause cannot be determined from the available evidence.";
        }

        if (serviceDegraded) {
            return "Service health degradation is confirmed, but the available evidence is insufficient to identify a specific root cause.";
        }

        if (metrics != null && !metrics.isEmpty()) {
            return "No specific root cause can be established from the available metric evidence.";
        }

        if (logs != null && !logs.isEmpty()) {
            return "No specific root cause can be established from the available log evidence.";
        }

        return "Insufficient evidence to determine a probable root cause.";
    }

    private String buildSummary(
            IncidentContext context,
            boolean hasServerErrors,
            boolean serviceDegraded) {

        String service = safeValue(
                context.service(),
                "unknown service"
        );

        if (hasServerErrors && serviceDegraded) {
            return service
                    + " is experiencing server-side errors and degraded health.";
        }

        if (hasServerErrors) {
            return service
                    + " is returning server-side HTTP errors.";
        }

        if (serviceDegraded) {
            return service
                    + " is reporting degraded health.";
        }

        return "The incident was analyzed using the available normalized evidence for "
                + service
                + ".";
    }

    private String safeValue(
            String value,
            String fallback) {

        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}
