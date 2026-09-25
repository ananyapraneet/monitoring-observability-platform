package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSummary;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LogSummaryGenerator {

    public List<LogSummary> summarize(
            List<RelevantLog> relevantLogs) {

        if (relevantLogs == null || relevantLogs.isEmpty()) {
            return List.of();
        }

        Map<LogClusterType, List<RelevantLog>> groupedLogs =
                new LinkedHashMap<>();

        for (RelevantLog relevantLog : relevantLogs) {

            if (relevantLog == null
                    || relevantLog.log() == null
                    || relevantLog.clusterType() == null) {
                continue;
            }

            groupedLogs
                    .computeIfAbsent(
                            relevantLog.clusterType(),
                            key -> new ArrayList<>()
                    )
                    .add(relevantLog);
        }

        List<LogSummary> summaries =
                new ArrayList<>();

        for (Map.Entry<LogClusterType, List<RelevantLog>> entry
                : groupedLogs.entrySet()) {

            LogClusterType clusterType =
                    entry.getKey();

            List<RelevantLog> logs =
                    entry.getValue();

            String service =
                    determineService(logs);

            String summary =
                    buildSummary(
                            service,
                            clusterType,
                            logs.size()
                    );

            summaries.add(
                    new LogSummary(
                            service,
                            clusterType,
                            logs.size(),
                            summary
                    )
            );
        }

        return List.copyOf(summaries);
    }

    private String determineService(
            List<RelevantLog> logs) {

        for (RelevantLog relevantLog : logs) {

            String service =
                    relevantLog.log().service();

            if (service != null && !service.isBlank()) {
                return service;
            }
        }

        return "unknown-service";
    }

    private String buildSummary(
            String service,
            LogClusterType clusterType,
            int occurrenceCount) {

        String serviceName =
                formatServiceName(service);

        String evidence =
                describeCluster(clusterType);

        if (occurrenceCount == 1) {
            return evidence
                    + " was observed in "
                    + serviceName
                    + ".";
        }

        return evidence
                + " errors were observed "
                + occurrenceCount
                + " times in "
                + serviceName
                + ".";
    }

    private String describeCluster(
            LogClusterType clusterType) {

        switch (clusterType) {

            case DATABASE_CONNECTION_EXHAUSTION:
                return "Database connection exhaustion";

            case DATABASE_CONNECTIVITY_FAILURE:
                return "Database connectivity failures";

            case HTTP_SERVER_FAILURE:
                return "HTTP 5xx server errors";

            case HTTP_CLIENT_FAILURE:
                return "HTTP 4xx client errors";

            case RESOURCE_EXHAUSTION:
                return "Resource exhaustion";

            case TIMEOUT_FAILURE:
                return "Timeout errors";

            case CONNECTION_FAILURE:
                return "Connection failures";

            case UNKNOWN:
            default:
                return "Unclassified errors";
        }
    }

    private String formatServiceName(
            String service) {

        if (service == null || service.isBlank()) {
            return "unknown service";
        }

        String[] parts =
                service.split("-");

        StringBuilder result =
                new StringBuilder();

        for (String part : parts) {

            if (part.isBlank()) {
                continue;
            }

            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(
                    Character.toUpperCase(
                            part.charAt(0)
                    )
            );

            if (part.length() > 1) {
                result.append(
                        part.substring(1)
                );
            }
        }

        return result.toString();
    }
}
