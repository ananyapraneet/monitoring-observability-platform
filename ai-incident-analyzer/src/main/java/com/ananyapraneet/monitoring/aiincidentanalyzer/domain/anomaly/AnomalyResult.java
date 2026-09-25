package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly;

public record AnomalyResult(
        AnomalyMetricType metricType,
        String service,
        double currentValue,
        double baselineMean,
        double baselineStandardDeviation,
        double zScore,
        boolean anomalous
) {
}
