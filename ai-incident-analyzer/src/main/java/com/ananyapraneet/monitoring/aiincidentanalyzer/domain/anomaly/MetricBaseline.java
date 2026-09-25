package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly;

public record MetricBaseline(
        AnomalyMetricType metricType,
        String service,
        double mean,
        double standardDeviation,
        int sampleCount
) {
}
