package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricBaseline;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricBaselineCalculatorTests {

    private final MetricBaselineCalculator calculator =
            new MetricBaselineCalculator();

    @Test
    void calculatesMeanAndStandardDeviation() {

        MetricBaseline baseline = calculator.calculate(
                AnomalyMetricType.LATENCY,
                "order-service",
                List.of(80.0, 90.0, 100.0, 110.0, 120.0)
        );

        assertEquals(100.0, baseline.mean());
        assertEquals(Math.sqrt(200.0), baseline.standardDeviation());
        assertEquals(5, baseline.sampleCount());
        assertEquals(
                AnomalyMetricType.LATENCY,
                baseline.metricType()
        );
        assertEquals("order-service", baseline.service());
    }

    @Test
    void calculatesBaselineForIdenticalValues() {

        MetricBaseline baseline = calculator.calculate(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                "order-service",
                List.of(25.0, 25.0, 25.0, 25.0)
        );

        assertEquals(25.0, baseline.mean());
        assertEquals(0.0, baseline.standardDeviation());
        assertEquals(4, baseline.sampleCount());
    }

    @Test
    void calculatesBaselineForRequestRate() {

        MetricBaseline baseline = calculator.calculate(
                AnomalyMetricType.REQUEST_RATE,
                "gateway",
                List.of(100.0, 110.0, 90.0, 100.0)
        );

        assertEquals(100.0, baseline.mean());
        assertEquals(7.0710678118654755, baseline.standardDeviation());
        assertEquals(4, baseline.sampleCount());
    }

    @Test
    void calculatesBaselineForErrorRate() {

        MetricBaseline baseline = calculator.calculate(
                AnomalyMetricType.ERROR_RATE,
                "gateway",
                List.of(1.0, 2.0, 1.0, 2.0)
        );

        assertEquals(1.5, baseline.mean());
        assertEquals(0.5, baseline.standardDeviation());
        assertEquals(4, baseline.sampleCount());
    }

    @Test
    void rejectsNullMetricType() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        null,
                        "order-service",
                        List.of(1.0, 2.0)
                )
        );
    }

    @Test
    void rejectsBlankService() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        AnomalyMetricType.LATENCY,
                        " ",
                        List.of(1.0, 2.0)
                )
        );
    }

    @Test
    void rejectsInsufficientHistoricalValues() {

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        List.of(100.0)
                )
        );
    }

    @Test
    void rejectsNullHistoricalValues() {

        List<Double> historicalValues = new java.util.ArrayList<>();

        historicalValues.add(100.0);
        historicalValues.add(null);
        historicalValues.add(120.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        historicalValues
                )
        );
    }

    @Test
    void rejectsEmptyHistoricalValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        List.of()
                )
        );
    }
}
