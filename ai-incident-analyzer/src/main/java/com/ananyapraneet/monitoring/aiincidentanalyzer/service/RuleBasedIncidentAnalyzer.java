package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AnalysisSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedIncident;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSummary;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.MetricLogCorrelation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.IncidentContextCorrelationAdapter;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentLogSummarizer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentMetricLogCorrelator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RuleBasedIncidentAnalyzer
        implements IncidentAnalyzer {

    private final CorrelationEngine correlationEngine;

    private final ConfidenceScorer confidenceScorer;

    private final IncidentContextCorrelationAdapter
            correlationAdapter;

    private final IncidentLogSummarizer logSummarizer;

    private final IncidentMetricLogCorrelator
            metricLogCorrelator;

    public RuleBasedIncidentAnalyzer(
            CorrelationEngine correlationEngine,
            ConfidenceScorer confidenceScorer) {

        this(
                correlationEngine,
                confidenceScorer,
                null,
                null,
                null
        );
    }

    public RuleBasedIncidentAnalyzer(
            CorrelationEngine correlationEngine,
            ConfidenceScorer confidenceScorer,
            IncidentContextCorrelationAdapter
                    correlationAdapter) {

        this(
                correlationEngine,
                confidenceScorer,
                correlationAdapter,
                null,
                null
        );
    }

    @Autowired
    public RuleBasedIncidentAnalyzer(
            CorrelationEngine correlationEngine,
            ConfidenceScorer confidenceScorer,
            IncidentContextCorrelationAdapter
                    correlationAdapter,
            IncidentLogSummarizer logSummarizer,
            IncidentMetricLogCorrelator
                    metricLogCorrelator) {

        this.correlationEngine =
                correlationEngine;

        this.confidenceScorer =
                confidenceScorer;

        this.correlationAdapter =
                correlationAdapter;

        this.logSummarizer =
                logSummarizer;

        this.metricLogCorrelator =
                metricLogCorrelator;
    }

    @Override
    public IncidentAnalysis analyze(
            IncidentContext context) {

        if (context == null) {

            return new IncidentAnalysis(
                    "Unknown Incident",
                    AnalysisSeverity.UNKNOWN,
                    "unknown",
                    "No incident context was available for analysis.",
                    correlationEngine.correlate(null),
                    List.of(),
                    "Insufficient evidence to determine a probable root cause.",
                    List.of(
                            "Collect incident context and retry the analysis."
                    ),
                    0.0
            );
        }

        List<Evidence> evidence =
                new ArrayList<>();

        List<String> remediation =
                new ArrayList<>();

        addAlertEvidence(
                context.alerts(),
                evidence
        );

        addMetricEvidence(
                context.metrics(),
                evidence
        );

        addAnomalyEvidence(
                context.anomalies(),
                evidence
        );

        addHttpErrorEvidence(
                context.httpErrors(),
                evidence
        );

        boolean serviceDegraded =
                isServiceDegraded(
                        context.health()
                );

        if (serviceDegraded) {

            evidence.add(
                    new Evidence(
                            "HEALTH",
                            "incident-context",
                            "Service health is reported as degraded."
                    )
            );

            remediation.add(
                    "Inspect the service health endpoint and failing health components."
            );
        }

        boolean hasServerErrors =
                hasServerErrors(
                        context.httpErrors()
                );

        if (hasServerErrors) {

            remediation.add(
                    "Inspect application errors and recent deployments for the affected service."
            );
        }

        if (hasServerErrors
                && serviceDegraded) {

            remediation.add(
                    "Check downstream dependencies and resource availability."
            );
        }

        AnalysisSeverity severity =
                determineSeverity(
                        context.severity()
                );

        String rootCause =
                determineRootCause(
                        hasServerErrors,
                        serviceDegraded,
                        context.metrics(),
                        context.logs(),
                        context.anomalies()
                );

        double confidence =
                confidenceScorer.score(
                        hasServerErrors,
                        serviceDegraded,
                        context.metrics(),
                        context.logs()
                );

        String summary =
                buildSummary(
                        context,
                        hasServerErrors,
                        serviceDegraded
                );

        Correlation correlation =
                correlationEngine.correlate(
                        context
                );

        CorrelatedIncident correlatedIncident =
                null;

        if (correlationAdapter != null) {

            correlatedIncident =
                    correlationAdapter.correlate(
                            context
                    );
        }

        addCorrelatedIncidentEvidence(
                correlatedIncident,
                evidence
        );

        addUnifiedAnomalyCorrelationEvidence(
                context.anomalies(),
                correlatedIncident,
                evidence
        );

        /*
         * Stage 15.8:
         *
         * Add normalized log summaries and metric ↔ log
         * correlation as additional incident evidence.
         *
         * These signals are descriptive evidence only.
         * They do not establish causation or a definitive
         * root cause.
         */

        addLogSummaryEvidence(
                context,
                evidence
        );

        addMetricLogCorrelationEvidence(
                context,
                evidence
        );

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

    private void addCorrelatedIncidentEvidence(
            CorrelatedIncident correlatedIncident,
            List<Evidence> evidence) {

        if (correlatedIncident == null) {
            return;
        }

        if (correlatedIncident.alerts() == null
                || correlatedIncident.alerts().isEmpty()) {
            return;
        }

        String description =
                "Stage 13 correlation identified "
                        + correlatedIncident.alerts().size()
                        + " correlated alert(s)";

        if (correlatedIncident.correlationTypes() != null
                && !correlatedIncident.correlationTypes().isEmpty()) {

            description +=
                    " using "
                            + correlatedIncident.correlationTypes()
                            + " evidence.";

        } else {

            description += ".";
        }

        evidence.add(
                new Evidence(
                        "CORRELATION",
                        "ai-incident-analyzer",
                        description
                )
        );
    }

    private void addUnifiedAnomalyCorrelationEvidence(
            List<AnomalyEvidence> anomalies,
            CorrelatedIncident correlatedIncident,
            List<Evidence> evidence) {

        if (anomalies == null
                || anomalies.isEmpty()) {
            return;
        }

        if (correlatedIncident == null) {
            return;
        }

        if (correlatedIncident.alerts() == null
                || correlatedIncident.alerts().isEmpty()) {
            return;
        }

        String correlationTypes =
                correlatedIncident.correlationTypes() == null
                        ? "[]"
                        : correlatedIncident.correlationTypes()
                                .toString();

        String description =
                "Anomalous metric behavior was detected alongside "
                        + "Stage 13 correlation evidence using "
                        + correlationTypes
                        + ". These signals indicate related abnormal "
                        + "behavior, but correlation does not by itself "
                        + "establish causation or a definitive root cause.";

        evidence.add(
                new Evidence(
                        "UNIFIED_CORRELATION",
                        "ai-incident-analyzer",
                        description
                )
        );
    }

    private void addLogSummaryEvidence(
            IncidentContext context,
            List<Evidence> evidence) {

        if (logSummarizer == null) {
            return;
        }

        List<LogSummary> summaries =
                logSummarizer.summarize(
                        context
                );

        if (summaries == null
                || summaries.isEmpty()) {
            return;
        }

        for (LogSummary summary : summaries) {

            if (summary == null) {
                continue;
            }

            String description =
                    summary.summary();

            if (description == null
                    || description.isBlank()) {

                description =
                        "Relevant "
                                + summary.clusterType()
                                + " log pattern observed "
                                + summary.occurrenceCount()
                                + " times in "
                                + summary.service()
                                + ".";
            }

            evidence.add(
                    new Evidence(
                            "LOG_SUMMARY",
                            "log-analysis",
                            description
                    )
            );
        }
    }

    private void addMetricLogCorrelationEvidence(
            IncidentContext context,
            List<Evidence> evidence) {

        if (metricLogCorrelator == null) {
            return;
        }

        List<MetricLogCorrelation> correlations =
                metricLogCorrelator.correlate(
                        context
                );

        if (correlations == null
                || correlations.isEmpty()) {
            return;
        }

        for (MetricLogCorrelation correlation
                : correlations) {

            if (correlation == null
                    || !correlation.correlated()) {
                continue;
            }

            String description =
                    correlation.summary();

            if (description == null
                    || description.isBlank()) {

                description =
                        correlation.metricType()
                                + " anomaly correlated with "
                                + correlation.clusterType()
                                + " logs observed "
                                + correlation.logOccurrenceCount()
                                + " times in "
                                + correlation.service()
                                + ".";
            }

            description +=
                    " This correlation is supporting evidence of related abnormal behavior, "
                            + "not proof that the metric anomaly caused the log pattern.";

            evidence.add(
                    new Evidence(
                            "METRIC_LOG_CORRELATION",
                            "log-analysis",
                            description
                    )
            );
        }
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

            String description =
                    alert.summary();

            if (description == null
                    || description.isBlank()) {

                description =
                        "Alert "
                                + alert.alertName()
                                + " is "
                                + alert.status()
                                + ".";
            }

            evidence.add(
                    new Evidence(
                            "ALERT",
                            "alertmanager",
                            description
                    )
            );
        }
    }

    private void addMetricEvidence(
            Map<String, Object> metrics,
            List<Evidence> evidence) {

        if (metrics == null
                || metrics.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry
                : metrics.entrySet()) {

            if (entry.getValue() == null) {
                continue;
            }

            evidence.add(
                    new Evidence(
                            "METRIC",
                            "prometheus",
                            entry.getKey()
                                    + " = "
                                    + entry.getValue()
                    )
            );
        }
    }

    private void addAnomalyEvidence(
            List<AnomalyEvidence> anomalies,
            List<Evidence> evidence) {

        if (anomalies == null) {
            return;
        }

        for (AnomalyEvidence anomaly : anomalies) {

            if (anomaly == null) {
                continue;
            }

            String description =
                    anomaly.metricType()
                            + " for "
                            + anomaly.service()
                            + " is anomalous relative to its historical baseline."
                            + " Current value = "
                            + anomaly.currentValue()
                            + ", baseline mean = "
                            + anomaly.baselineMean()
                            + ", standard deviation = "
                            + anomaly.baselineStandardDeviation()
                            + ", z-score = "
                            + anomaly.zScore()
                            + ". This is supporting evidence of abnormal behavior,"
                            + " not proof of root cause.";

            evidence.add(
                    new Evidence(
                            "ANOMALY",
                            "prometheus-anomaly-detector",
                            description
                    )
            );
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

            evidence.add(
                    new Evidence(
                            "HTTP_ERROR",
                            "incident-context",
                            error.method()
                                    + " "
                                    + error.endpoint()
                                    + " returned HTTP "
                                    + error.status()
                    )
            );
        }
    }

    private boolean hasServerErrors(
            List<HttpErrorEvidence> errors) {

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

    private boolean isServiceDegraded(
            HealthEvidence health) {

        if (health == null
                || health.status() == null) {

            return false;
        }

        return !health.status()
                .equalsIgnoreCase("UP");
    }

    private AnalysisSeverity determineSeverity(
            String severity) {

        if (severity == null
                || severity.isBlank()) {

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
            List<?> logs,
            List<AnomalyEvidence> anomalies) {

        if (hasServerErrors
                && serviceDegraded) {

            return "Application or dependency failure is suspected, but the available evidence is insufficient to identify a specific root cause.";
        }

        if (hasServerErrors) {

            return "Server-side application failure is indicated by HTTP 5xx errors; the specific root cause cannot be determined from the available evidence.";
        }

        if (serviceDegraded) {

            return "Service health degradation is confirmed, but the available evidence is insufficient to identify a specific root cause.";
        }

        if (anomalies != null
                && !anomalies.isEmpty()) {

            return "Anomalous metric behavior was detected relative to the historical baseline, but the anomaly alone does not establish the root cause.";
        }

        if (metrics != null
                && !metrics.isEmpty()) {

            return "No specific root cause can be established from the available metric evidence.";
        }

        if (logs != null
                && !logs.isEmpty()) {

            return "No specific root cause can be established from the available log evidence.";
        }

        return "Insufficient evidence to determine a probable root cause.";
    }

    private String buildSummary(
            IncidentContext context,
            boolean hasServerErrors,
            boolean serviceDegraded) {

        String service =
                safeValue(
                        context.service(),
                        "unknown service"
                );

        if (hasServerErrors
                && serviceDegraded) {

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

        if (context.anomalies() != null
                && !context.anomalies().isEmpty()) {

            if (context.anomalies().size() == 1) {

                AnomalyEvidence anomaly =
                        context.anomalies().get(0);

                return service
                        + " has anomalous "
                        + anomaly.metricType()
                        + " relative to its historical baseline.";
            }

            return service
                    + " has "
                    + context.anomalies().size()
                    + " anomalous metrics relative to their historical baselines.";
        }

        return "The incident was analyzed using the available normalized evidence for "
                + service
                + ".";
    }

    private String safeValue(
            String value,
            String fallback) {

        if (value == null
                || value.isBlank()) {

            return fallback;
        }

        return value;
    }
}
