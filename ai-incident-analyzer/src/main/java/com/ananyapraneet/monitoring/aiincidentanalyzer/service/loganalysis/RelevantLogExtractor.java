package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorCluster;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RelevantLogExtractor {

    private final ErrorClusterer errorClusterer;

    public RelevantLogExtractor(
            ErrorClusterer errorClusterer) {

        if (errorClusterer == null) {
            throw new IllegalArgumentException(
                    "errorClusterer must not be null"
            );
        }

        this.errorClusterer = errorClusterer;
    }

    public List<RelevantLog> extract(
            List<NormalizedLog> logs,
            Set<LogClusterType> relevantClusterTypes,
            String service) {

        if (logs == null || logs.isEmpty()) {
            return List.of();
        }

        if (relevantClusterTypes == null
                || relevantClusterTypes.isEmpty()) {
            return List.of();
        }

        Set<LogClusterType> requestedClusters =
                new HashSet<>(relevantClusterTypes);

        List<NormalizedLog> serviceLogs =
                filterByService(logs, service);

        if (serviceLogs.isEmpty()) {
            return List.of();
        }

        List<ErrorCluster> clusters =
                errorClusterer.cluster(serviceLogs);

        List<RelevantLog> relevantLogs =
                new ArrayList<>();

        for (ErrorCluster cluster : clusters) {

            if (!requestedClusters.contains(
                    cluster.clusterType())) {
                continue;
            }

            for (NormalizedLog log : cluster.logs()) {

                relevantLogs.add(
                        new RelevantLog(
                                log,
                                cluster.clusterType(),
                                "Matches a log cluster relevant to the current incident"
                        )
                );
            }
        }

        return List.copyOf(relevantLogs);
    }

    private List<NormalizedLog> filterByService(
            List<NormalizedLog> logs,
            String service) {

        if (service == null || service.isBlank()) {
            return List.copyOf(logs);
        }

        List<NormalizedLog> matchingLogs =
                new ArrayList<>();

        for (NormalizedLog log : logs) {

            if (log == null) {
                continue;
            }

            if (service.equals(log.service())) {
                matchingLogs.add(log);
            }
        }

        return matchingLogs;
    }
}
