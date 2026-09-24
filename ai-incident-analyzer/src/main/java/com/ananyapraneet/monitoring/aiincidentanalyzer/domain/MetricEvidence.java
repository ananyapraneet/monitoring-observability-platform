package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.time.Instant;

public record MetricEvidence(
        String metricName,
        String service,
        String resource,
        double value,
        double threshold,
        Instant timestamp
) {}
