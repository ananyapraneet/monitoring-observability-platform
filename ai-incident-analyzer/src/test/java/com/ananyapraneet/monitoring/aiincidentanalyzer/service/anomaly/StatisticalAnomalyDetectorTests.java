package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricBaseline;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricObservation;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatisticalAnomalyDetectorTests {

    private final StatisticalAnomalyDetector detector =
            new StatisticalAnomalyDetector();

    @Test
    void detectsAnomalousValue() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                150.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(5.0, result.zScore());
        assertTrue(result.anomalous());
    }

    @Test
    void identifiesNormalValue() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                100.0,
                20.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                120.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(1.0, result.zScore());
        assertFalse(result.anomalous());
    }

    @Test
    void detectsAnomalyWhenValueIsBelowBaseline() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                50.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(5.0, result.zScore());
        assertTrue(result.anomalous());
    }

    @Test
    void handlesZeroStandardDeviationWhenValueMatchesMean() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                25.0,
                0.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                25.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(0.0, result.zScore());
        assertFalse(result.anomalous());
    }

    @Test
    void handlesZeroStandardDeviationWhenValueChanges() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                25.0,
                0.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                30.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertTrue(Double.isInfinite(result.zScore()));
        assertTrue(result.anomalous());
    }

    @Test
    void rejectsInsufficientBaselineSamples() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.ERROR_RATE,
                "gateway",
                2.0,
                1.0,
                1
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.ERROR_RATE,
                "gateway",
                5.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> detector.detect(baseline, observation)
        );
    }

    @Test
    void rejectsNullBaseline() {

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                150.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> detector.detect(null, observation)
        );
    }

    @Test
    void rejectsNullObservation() {

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> detector.detect(baseline, null)
        );
    }

    @Test
    void supportsCustomZScoreThreshold() {

        StatisticalAnomalyDetector customDetector =
                new StatisticalAnomalyDetector(2.0);

        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                125.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result =
                customDetector.detect(baseline, observation);

        assertEquals(2.5, result.zScore());
        assertTrue(result.anomalous());
        assertEquals(2.0, customDetector.getZScoreThreshold());
    }

    @Test
    void treatsExactThresholdAsAnomaly() {
        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                130.0,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(3.0, result.zScore());
        assertTrue(result.anomalous());
    }

    @Test
    void doesNotFlagValueJustBelowThreshold() {
        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                129.9,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(2.99, result.zScore(), 0.0000001);
        assertFalse(result.anomalous());
    }

    @Test
    void flagsValueJustAboveThreshold() {
        MetricBaseline baseline = new MetricBaseline(
                AnomalyMetricType.LATENCY,
                "order-service",
                100.0,
                10.0,
                30
        );

        MetricObservation observation = new MetricObservation(
                AnomalyMetricType.LATENCY,
                "order-service",
                130.1,
                Instant.parse("2026-09-25T00:00:00Z")
        );

        AnomalyResult result = detector.detect(baseline, observation);

        assertEquals(3.01, result.zScore(), 0.0000001);
        assertTrue(result.anomalous());
    }

    @Test
    void rejectsNonPositiveZScoreThreshold() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StatisticalAnomalyDetector(0.0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new StatisticalAnomalyDetector(-1.0)
        );
    }
}
