package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricBaseline;

import java.util.List;

public class MetricBaselineCalculator {

    public MetricBaseline calculate(
            AnomalyMetricType metricType,
            String service,
            List<Double> historicalValues) {

        if (metricType == null) {
            throw new IllegalArgumentException("Metric type must not be null");
        }

        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("Service must not be blank");
        }

        if (historicalValues == null || historicalValues.size() < 2) {
            throw new IllegalArgumentException(
                    "At least two historical values are required");
        }

        double sum = 0.0;

        for (Double value : historicalValues) {
            if (value == null) {
                throw new IllegalArgumentException(
                        "Historical values must not contain null");
            }

            sum += value;
        }

        double mean = sum / historicalValues.size();

        double squaredDifferenceSum = 0.0;

        for (Double value : historicalValues) {
            double difference = value - mean;
            squaredDifferenceSum += difference * difference;
        }

        double variance =
                squaredDifferenceSum / historicalValues.size();

        double standardDeviation = Math.sqrt(variance);

        return new MetricBaseline(
                metricType,
                service,
                mean,
                standardDeviation,
                historicalValues.size()
        );
    }
}
