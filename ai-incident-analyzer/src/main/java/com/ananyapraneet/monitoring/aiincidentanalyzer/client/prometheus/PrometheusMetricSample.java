package com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus;

import java.time.Instant;

public record PrometheusMetricSample(
        double value,
        Instant timestamp
) {
}
