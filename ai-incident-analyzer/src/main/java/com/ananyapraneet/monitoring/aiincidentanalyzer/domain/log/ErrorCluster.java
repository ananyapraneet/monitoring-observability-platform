package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

import java.util.List;

public record ErrorCluster(
        LogClusterType clusterType,
        int occurrenceCount,
        List<ErrorPattern> errorPatterns,
        List<NormalizedLog> logs) {
}
