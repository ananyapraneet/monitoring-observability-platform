package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetricCorrelator {

    public MetricCorrelation correlate(
            AlertEvidence alert,
            List<MetricEvidence> metrics
    ) {

        if (alert == null) {
            return new MetricCorrelation(
                    null,
                    List.of(),
                    0.0
            );
        }

        if (metrics == null || metrics.isEmpty()) {
            return new MetricCorrelation(
                    alert.alertName(),
                    List.of(),
                    0.0
            );
        }

        List<MetricEvidence> relatedMetrics =
                new ArrayList<>();

        for (MetricEvidence metric : metrics) {

            if (metric == null) {
                continue;
            }

            if (!matchesService(alert, metric)) {
                continue;
            }

            if (!matchesResource(alert, metric)) {
                continue;
            }

            if (!isThresholdBreached(metric)) {
                continue;
            }

            relatedMetrics.add(metric);
        }

        double correlationScore =
                calculateScore(relatedMetrics);

        return new MetricCorrelation(
                alert.alertName(),
                List.copyOf(relatedMetrics),
                correlationScore
        );
    }

    private boolean matchesService(
            AlertEvidence alert,
            MetricEvidence metric
    ) {

        if (alert.service() == null
                || metric.service() == null) {
            return false;
        }

        return alert.service()
                .trim()
                .equalsIgnoreCase(
                        metric.service().trim()
                );
    }

    private boolean matchesResource(
            AlertEvidence alert,
            MetricEvidence metric
    ) {

        if (metric.resource() == null
                || metric.resource().isBlank()) {
            return true;
        }

        if (alert.instance() == null
                || alert.instance().isBlank()) {
            return false;
        }

        return alert.instance()
                .trim()
                .equalsIgnoreCase(
                        metric.resource().trim()
                );
    }

    private boolean isThresholdBreached(
            MetricEvidence metric
    ) {
        return metric.value() >= metric.threshold();
    }

    private double calculateScore(
            List<MetricEvidence> relatedMetrics
    ) {

        if (relatedMetrics.isEmpty()) {
            return 0.0;
        }

        if (relatedMetrics.size() == 1) {
            return 0.5;
        }

        if (relatedMetrics.size() == 2) {
            return 0.75;
        }

        return 1.0;
    }
}
