package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly;

import java.time.Instant;

public record MetricObservation(
        AnomalyMetricType metricType,
        String service,
        double value,
        Instant timestamp
) {
}
