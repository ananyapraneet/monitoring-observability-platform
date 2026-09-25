package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricBaseline;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.MetricObservation;

public class StatisticalAnomalyDetector {

    private static final double DEFAULT_Z_SCORE_THRESHOLD = 3.0;

    private final double zScoreThreshold;

    public StatisticalAnomalyDetector() {
        this(DEFAULT_Z_SCORE_THRESHOLD);
    }

    public StatisticalAnomalyDetector(double zScoreThreshold) {

        if (zScoreThreshold <= 0) {
            throw new IllegalArgumentException(
                    "Z-score threshold must be greater than zero");
        }

        this.zScoreThreshold = zScoreThreshold;
    }

    public AnomalyResult detect(
            MetricBaseline baseline,
            MetricObservation observation) {

        if (baseline == null) {
            throw new IllegalArgumentException("Baseline must not be null");
        }

        if (observation == null) {
            throw new IllegalArgumentException("Observation must not be null");
        }

        if (baseline.sampleCount() < 2) {
            throw new IllegalArgumentException(
                    "Baseline must contain at least two samples");
        }

        if (baseline.standardDeviation() < 0) {
            throw new IllegalArgumentException(
                    "Standard deviation must not be negative");
        }

        double currentValue = observation.value();
        double mean = baseline.mean();
        double standardDeviation = baseline.standardDeviation();

        double zScore;

        if (standardDeviation == 0) {
            if (currentValue == mean) {
                zScore = 0.0;
            } else {
                zScore = Double.POSITIVE_INFINITY;
            }
        } else {
            zScore = Math.abs(currentValue - mean) / standardDeviation;
        }

        boolean anomalous = zScore >= zScoreThreshold;

        return new AnomalyResult(
                observation.metricType(),
                observation.service(),
                currentValue,
                mean,
                standardDeviation,
                zScore,
                anomalous
        );
    }

    public double getZScoreThreshold() {
        return zScoreThreshold;
    }
}
