package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

public record RelevantLog(
        NormalizedLog log,
        LogClusterType clusterType,
        String relevanceReason) {
}
