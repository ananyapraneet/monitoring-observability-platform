package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;

import java.time.Instant;

public record AnomalyEvidence(

        AnomalyMetricType metricType,

        String service,

        double currentValue,

        double baselineMean,

        double baselineStandardDeviation,

        double zScore,

        boolean anomalous,

        Instant timestamp

) {
}
