package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricSample;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricsClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PrometheusAnomalyDetectionService {

    private final PrometheusMetricsClient metricsClient;
    private final MetricAnomalyDetectionService anomalyDetectionService;

    public PrometheusAnomalyDetectionService(
            PrometheusMetricsClient metricsClient) {

        this.metricsClient = metricsClient;
        this.anomalyDetectionService =
                new MetricAnomalyDetectionService();
    }

    public PrometheusAnomalyDetectionService(
            PrometheusMetricsClient metricsClient,
            double zScoreThreshold) {

        this.metricsClient = metricsClient;
        this.anomalyDetectionService =
                new MetricAnomalyDetectionService(
                        zScoreThreshold
                );
    }

    public AnomalyResult detect(
            AnomalyMetricType metricType,
            String service,
            String query,
            Instant end,
            Duration historicalWindow,
            long stepSeconds) {

        Instant start = end.minus(historicalWindow);

        List<PrometheusMetricSample> historicalSamples =
                metricsClient.queryRange(
                        query,
                        start,
                        end,
                        stepSeconds
                );

        if (historicalSamples.size() < 2) {
            throw new IllegalArgumentException(
                    "Prometheus must return at least two historical samples"
            );
        }

        List<Double> historicalValues =
                new ArrayList<>();

        for (PrometheusMetricSample sample : historicalSamples) {
            historicalValues.add(sample.value());
        }

        PrometheusMetricSample currentSample =
                metricsClient.queryInstant(
                        query,
                        end
                );

        return anomalyDetectionService.detect(
                metricType,
                service,
                historicalValues,
                currentSample.value(),
                currentSample.timestamp()
        );
    }
}
