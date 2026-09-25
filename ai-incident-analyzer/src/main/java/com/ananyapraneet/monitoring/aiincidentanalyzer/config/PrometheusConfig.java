package com.ananyapraneet.monitoring.aiincidentanalyzer.config;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricsClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.RestPrometheusMetricsClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly.PrometheusAnomalyDetectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PrometheusConfig {

    @Bean
    public PrometheusMetricsClient prometheusMetricsClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${prometheus.base-url}") String prometheusBaseUrl) {

        return new RestPrometheusMetricsClient(
                restClientBuilder,
                prometheusBaseUrl,
                objectMapper
        );
    }

    @Bean
    public PrometheusAnomalyDetectionService prometheusAnomalyDetectionService(
            PrometheusMetricsClient prometheusMetricsClient) {

        return new PrometheusAnomalyDetectionService(
                prometheusMetricsClient
        );
    }
}
