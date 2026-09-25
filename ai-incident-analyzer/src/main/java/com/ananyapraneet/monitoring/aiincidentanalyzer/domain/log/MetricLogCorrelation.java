package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;

public record MetricLogCorrelation(
        AnomalyMetricType metricType,
        LogClusterType clusterType,
        String service,
        boolean correlated,
        int logOccurrenceCount,
        String summary) {
}
