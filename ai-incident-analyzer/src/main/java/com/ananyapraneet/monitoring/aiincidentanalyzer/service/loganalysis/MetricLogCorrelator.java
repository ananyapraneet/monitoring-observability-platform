package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.MetricLogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MetricLogCorrelator {

    private static final Duration DEFAULT_CORRELATION_WINDOW =
            Duration.ofMinutes(5);

    private final Duration correlationWindow;

    public MetricLogCorrelator() {
        this(DEFAULT_CORRELATION_WINDOW);
    }

    public MetricLogCorrelator(
            Duration correlationWindow) {

        if (correlationWindow == null
                || correlationWindow.isNegative()) {
            throw new IllegalArgumentException(
                    "correlationWindow must not be null or negative"
            );
        }

        this.correlationWindow =
                correlationWindow;
    }

    public List<MetricLogCorrelation> correlate(
            List<AnomalyEvidence> anomalies,
            List<RelevantLog> relevantLogs) {

        if (anomalies == null || anomalies.isEmpty()) {
            return List.of();
        }

        if (relevantLogs == null || relevantLogs.isEmpty()) {
            return List.of();
        }

        List<MetricLogCorrelation> correlations =
                new ArrayList<>();

        for (AnomalyEvidence anomaly : anomalies) {

            if (anomaly == null || !anomaly.anomalous()) {
                continue;
            }

            LogClusterType expectedCluster =
                    determineExpectedCluster(
                            anomaly.metricType()
                    );

            if (expectedCluster == null) {
                continue;
            }

            List<RelevantLog> matchingLogs =
                    findMatchingLogs(
                            anomaly,
                            expectedCluster,
                            relevantLogs
                    );

            if (matchingLogs.isEmpty()) {
                continue;
            }

            String service =
                    determineService(
                            anomaly,
                            matchingLogs
                    );

            String summary =
                    buildSummary(
                            anomaly.metricType(),
                            expectedCluster,
                            service,
                            matchingLogs.size()
                    );

            correlations.add(
                    new MetricLogCorrelation(
                            anomaly.metricType(),
                            expectedCluster,
                            service,
                            true,
                            matchingLogs.size(),
                            summary
                    )
            );
        }

        return List.copyOf(correlations);
    }

    private List<RelevantLog> findMatchingLogs(
            AnomalyEvidence anomaly,
            LogClusterType expectedCluster,
            List<RelevantLog> relevantLogs) {

        List<RelevantLog> matchingLogs =
                new ArrayList<>();

        for (RelevantLog relevantLog : relevantLogs) {

            if (relevantLog == null
                    || relevantLog.clusterType() == null
                    || relevantLog.log() == null) {
                continue;
            }

            if (relevantLog.clusterType()
                    != expectedCluster) {
                continue;
            }

            if (!isTemporallyAligned(
                    anomaly.timestamp(),
                    relevantLog.log().timestamp())) {
                continue;
            }

            matchingLogs.add(relevantLog);
        }

        return matchingLogs;
    }

    private boolean isTemporallyAligned(
            Instant anomalyTimestamp,
            Instant logTimestamp) {

        if (anomalyTimestamp == null
                || logTimestamp == null) {
            return false;
        }

        Duration difference =
                Duration.between(
                        anomalyTimestamp,
                        logTimestamp
                ).abs();

        return difference.compareTo(
                correlationWindow
        ) <= 0;
    }

    private LogClusterType determineExpectedCluster(
            AnomalyMetricType metricType) {

        if (metricType == null) {
            return null;
        }

        switch (metricType) {

            case DATABASE_CONNECTIONS:
                return LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION;

            case ERROR_RATE:
                return LogClusterType
                        .HTTP_SERVER_FAILURE;

            case LATENCY:
                return LogClusterType
                        .TIMEOUT_FAILURE;

            case REQUEST_RATE:
            default:
                return null;
        }
    }

    private String determineService(
            AnomalyEvidence anomaly,
            List<RelevantLog> matchingLogs) {

        if (anomaly.service() != null
                && !anomaly.service().isBlank()) {
            return anomaly.service();
        }

        for (RelevantLog relevantLog : matchingLogs) {

            String service =
                    relevantLog.log().service();

            if (service != null && !service.isBlank()) {
                return service;
            }
        }

        return "unknown-service";
    }

    private String buildSummary(
            AnomalyMetricType metricType,
            LogClusterType clusterType,
            String service,
            int occurrenceCount) {

        return metricType
                + " anomaly correlated with "
                + clusterType
                + " logs observed "
                + occurrenceCount
                + " times in "
                + service
                + ".";
    }
}
