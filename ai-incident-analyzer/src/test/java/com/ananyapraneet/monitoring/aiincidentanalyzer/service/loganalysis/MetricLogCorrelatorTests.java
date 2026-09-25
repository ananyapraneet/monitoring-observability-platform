package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorCluster;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.MetricLogCorrelation;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetricLogCorrelatorTests {

    private final Instant anomalyTimestamp =
            Instant.parse("2026-09-25T10:00:00Z");

    @Test
    void correlatesAnomalyWithMatchingLogWithinFiveMinutes() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:04:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertEquals(1, correlations.size());
        assertTrue(correlations.get(0).correlated());
        assertEquals(
                LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                correlations.get(0).clusterType()
        );
    }

    @Test
    void correlatesLogBeforeAnomalyWithinFiveMinutes() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T09:56:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertEquals(1, correlations.size());
    }

    @Test
    void includesExactFiveMinuteBoundary() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:05:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertEquals(1, correlations.size());
    }

    @Test
    void excludesLogOutsideFiveMinuteWindow() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:06:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    @Test
    void excludesLogWithNullTimestamp() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        null
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    @Test
    void excludesAnomalyWithNullTimestamp() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        null
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:01:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    @Test
    void supportsCustomCorrelationWindow() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:03:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator(
                        Duration.ofMinutes(2)
                );

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    @Test
    void rejectsNullCorrelationWindow() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new MetricLogCorrelator(null)
        );
    }

    @Test
    void rejectsNegativeCorrelationWindow() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new MetricLogCorrelator(
                        Duration.ofMinutes(-1)
                )
        );
    }

    @Test
    void doesNotCorrelateRequestRateBecauseNoKnownRelationshipExists() {

        AnomalyEvidence anomaly =
                anomaly(
                        AnomalyMetricType.REQUEST_RATE,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.HTTP_SERVER_FAILURE,
                        "2026-09-25T10:01:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    @Test
    void doesNotCorrelateNonAnomalousMetric() {

        AnomalyEvidence anomaly =
                new AnomalyEvidence(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        "order-service",
                        0.5,
                        2.0,
                        1.0,
                        0.5,
                        false,
                        anomalyTimestamp
                );

        RelevantLog log =
                relevantLog(
                        LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                        "2026-09-25T10:01:00Z"
                );

        MetricLogCorrelator correlator =
                new MetricLogCorrelator();

        List<MetricLogCorrelation> correlations =
                correlator.correlate(
                        List.of(anomaly),
                        List.of(log)
                );

        assertTrue(correlations.isEmpty());
    }

    private AnomalyEvidence anomaly(
            AnomalyMetricType metricType,
            Instant timestamp) {

        return new AnomalyEvidence(
                metricType,
                "order-service",
                4.0,
                2.0,
                1.0,
                4.0,
                true,
                timestamp
        );
    }

    private RelevantLog relevantLog(
            LogClusterType clusterType,
            String timestamp) {

        Instant instant =
                timestamp == null
                        ? null
                        : Instant.parse(timestamp);

        NormalizedLog normalizedLog =
                new NormalizedLog(
                        "order-service",
                        LogSeverity.ERROR,
                        "original error",
                        "normalized error",
                        null,
                        instant
                );

        return new RelevantLog(
                normalizedLog,
                clusterType,
                "relevant to incident"
        );
    }
}
