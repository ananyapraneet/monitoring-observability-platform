package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricBaseline;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricObservation;

import java.time.Instant;
import java.util.List;

public class MetricAnomalyDetectionService {

    private final MetricBaselineCalculator baselineCalculator;
    private final StatisticalAnomalyDetector anomalyDetector;

    public MetricAnomalyDetectionService() {
        this.baselineCalculator = new MetricBaselineCalculator();
        this.anomalyDetector = new StatisticalAnomalyDetector();
    }

    public MetricAnomalyDetectionService(double zScoreThreshold) {
        this.baselineCalculator = new MetricBaselineCalculator();
        this.anomalyDetector =
                new StatisticalAnomalyDetector(zScoreThreshold);
    }

    public AnomalyResult detect(
            AnomalyMetricType metricType,
            String service,
            List<Double> historicalValues,
            double currentValue,
            Instant timestamp) {

        MetricBaseline baseline = baselineCalculator.calculate(
                metricType,
                service,
                historicalValues
        );

        MetricObservation observation = new MetricObservation(
                metricType,
                service,
                currentValue,
                timestamp
        );

        return anomalyDetector.detect(
                baseline,
                observation
        );
    }
}
