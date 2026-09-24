package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record MetricCorrelation(
        String alertName,
        List<MetricEvidence> relatedMetrics,
        double correlationScore
) {}
