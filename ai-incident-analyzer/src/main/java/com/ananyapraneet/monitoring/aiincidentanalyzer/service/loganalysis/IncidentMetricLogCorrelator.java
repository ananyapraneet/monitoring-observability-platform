package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.MetricLogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.util.List;

public class IncidentMetricLogCorrelator {

    private final IncidentRelevantLogExtractor relevantLogExtractor;
    private final MetricLogCorrelator metricLogCorrelator;

    public IncidentMetricLogCorrelator(
            IncidentRelevantLogExtractor relevantLogExtractor,
            MetricLogCorrelator metricLogCorrelator) {

        if (relevantLogExtractor == null) {
            throw new IllegalArgumentException(
                    "relevantLogExtractor must not be null"
            );
        }

        if (metricLogCorrelator == null) {
            throw new IllegalArgumentException(
                    "metricLogCorrelator must not be null"
            );
        }

        this.relevantLogExtractor =
                relevantLogExtractor;

        this.metricLogCorrelator =
                metricLogCorrelator;
    }

    public List<MetricLogCorrelation> correlate(
            IncidentContext context) {

        if (context == null) {
            return List.of();
        }

        List<RelevantLog> relevantLogs =
                relevantLogExtractor.extract(context);

        if (relevantLogs.isEmpty()) {
            return List.of();
        }

        return metricLogCorrelator.correlate(
                context.anomalies(),
                relevantLogs
        );
    }
}
