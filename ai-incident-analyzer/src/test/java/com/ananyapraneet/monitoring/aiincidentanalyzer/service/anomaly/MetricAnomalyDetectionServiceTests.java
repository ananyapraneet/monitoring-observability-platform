package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricAnomalyDetectionServiceTests {

    private final MetricAnomalyDetectionService service =
            new MetricAnomalyDetectionService();

    @Test
    void detectsHighLatencyAnomaly() {

        AnomalyResult result = service.detect(
                AnomalyMetricType.LATENCY,
                "order-service",
                List.of(
                        80.0,
                        90.0,
                        100.0,
                        110.0,
                        120.0
                ),
                450.0,
                Instant.parse("2026-09-25T10:00:00Z")
        );

        assertTrue(result.anomalous());
        assertTrue(result.zScore() >= 3.0);
        assertTrue(result.currentValue() == 450.0);
        assertTrue(result.baselineMean() == 100.0);
    }

    @Test
    void identifiesNormalLatency() {

        AnomalyResult result = service.detect(
                AnomalyMetricType.LATENCY,
                "order-service",
                List.of(
                        80.0,
                        90.0,
                        100.0,
                        110.0,
                        120.0
                ),
                105.0,
                Instant.parse("2026-09-25T10:01:00Z")
        );

        assertFalse(result.anomalous());
        assertTrue(result.zScore() < 3.0);
    }

    @Test
    void detectsHighDatabaseConnections() {

        AnomalyResult result = service.detect(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                List.of(
                        20.0,
                        21.0,
                        22.0,
                        21.0,
                        20.0
                ),
                50.0,
                Instant.parse("2026-09-25T10:02:00Z")
        );

        assertTrue(result.anomalous());
    }

    @Test
    void detectsHighErrorRate() {

        AnomalyResult result = service.detect(
                AnomalyMetricType.ERROR_RATE,
                "gateway",
                List.of(
                        1.0,
                        1.2,
                        0.8,
                        1.1,
                        0.9
                ),
                5.0,
                Instant.parse("2026-09-25T10:03:00Z")
        );

        assertTrue(result.anomalous());
    }

    @Test
    void detectsLowRequestRateAsAnomaly() {

        AnomalyResult result = service.detect(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                List.of(
                        100.0,
                        105.0,
                        95.0,
                        102.0,
                        98.0
                ),
                40.0,
                Instant.parse("2026-09-25T10:04:00Z")
        );

        assertTrue(result.anomalous());
    }

    @Test
    void supportsCustomZScoreThreshold() {

        MetricAnomalyDetectionService customService =
                new MetricAnomalyDetectionService(2.0);

        AnomalyResult result = customService.detect(
                AnomalyMetricType.LATENCY,
                "order-service",
                List.of(
                        80.0,
                        90.0,
                        100.0,
                        110.0,
                        120.0
                ),
                130.0,
                Instant.parse("2026-09-25T10:05:00Z")
        );

        assertTrue(result.anomalous());
    }
}
