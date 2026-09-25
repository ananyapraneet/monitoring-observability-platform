package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorCluster;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ErrorClusterer {

    public List<ErrorCluster> cluster(
            List<NormalizedLog> logs) {

        if (logs == null || logs.isEmpty()) {
            return List.of();
        }

        Map<LogClusterType, List<NormalizedLog>> groupedLogs =
                new LinkedHashMap<>();

        Map<LogClusterType, Set<ErrorPattern>> groupedPatterns =
                new LinkedHashMap<>();

        for (NormalizedLog log : logs) {
            if (log == null || log.errorPattern() == null) {
                continue;
            }

            ErrorPattern errorPattern = log.errorPattern();

            LogClusterType clusterType =
                    determineClusterType(errorPattern);

            if (clusterType == LogClusterType.UNKNOWN) {
                continue;
            }

            groupedLogs
                    .computeIfAbsent(
                            clusterType,
                            key -> new ArrayList<>()
                    )
                    .add(log);

            groupedPatterns
                    .computeIfAbsent(
                            clusterType,
                            key -> new LinkedHashSet<>()
                    )
                    .add(errorPattern);
        }

        List<ErrorCluster> clusters = new ArrayList<>();

        for (Map.Entry<LogClusterType, List<NormalizedLog>> entry
                : groupedLogs.entrySet()) {

            LogClusterType clusterType = entry.getKey();
            List<NormalizedLog> matchingLogs = entry.getValue();

            Set<ErrorPattern> patterns =
                    groupedPatterns.get(clusterType);

            clusters.add(
                    new ErrorCluster(
                            clusterType,
                            matchingLogs.size(),
                            List.copyOf(patterns),
                            List.copyOf(matchingLogs)
                    )
            );
        }

        return List.copyOf(clusters);
    }

    private LogClusterType determineClusterType(
            ErrorPattern errorPattern) {

        switch (errorPattern) {
            case CONNECTION_POOL_EXHAUSTED:
            case DATABASE_CONNECTION_FAILURE:
            case DATABASE_CONNECTION_TIMEOUT:
                return LogClusterType.DATABASE_CONNECTION_EXHAUSTION;

            case CONNECTION_REFUSED:
                return LogClusterType.CONNECTION_FAILURE;

            case HTTP_5XX:
                return LogClusterType.HTTP_SERVER_FAILURE;

            case HTTP_4XX:
                return LogClusterType.HTTP_CLIENT_FAILURE;

            case OUT_OF_MEMORY:
                return LogClusterType.RESOURCE_EXHAUSTION;

            case TIMEOUT:
                return LogClusterType.TIMEOUT_FAILURE;

            case UNKNOWN:
            default:
                return LogClusterType.UNKNOWN;
        }
    }
}
