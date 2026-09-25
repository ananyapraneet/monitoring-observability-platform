package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

public record LogSummary(
        String service,
        LogClusterType clusterType,
        int occurrenceCount,
        String summary) {
}
