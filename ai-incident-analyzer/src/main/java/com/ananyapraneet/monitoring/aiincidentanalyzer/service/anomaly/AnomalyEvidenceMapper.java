package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;

public class AnomalyEvidenceMapper {

    public Evidence map(AnomalyResult result) {

        if (result == null) {
            return null;
        }

        String description =
                result.metricType()
                        + " for "
                        + result.service()
                        + " is "
                        + (result.anomalous()
                        ? "significantly different from its baseline"
                        : "within its baseline")
                        + ". Current value = "
                        + result.currentValue()
                        + ", baseline mean = "
                        + result.baselineMean()
                        + ", standard deviation = "
                        + result.baselineStandardDeviation()
                        + ", z-score = "
                        + result.zScore()
                        + ".";

        return new Evidence(
                "ANOMALY",
                "prometheus-anomaly-detector",
                description
        );
    }
}
