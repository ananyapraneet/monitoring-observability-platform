package com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrometheusLocalIntegrationTests {

    @Test
    void queriesRealPrometheus() {

        RestPrometheusMetricsClient client =
                new RestPrometheusMetricsClient(
                        RestClient.builder(),
                        "http://localhost:9090",
                        new ObjectMapper()
                );

        List<PrometheusMetricSample> samples =
                client.queryRange(
                        "up",
                        Instant.now().minusSeconds(300),
                        Instant.now(),
                        60
                );

        assertNotNull(samples);
        assertFalse(samples.isEmpty());

        boolean containsHealthyTarget = false;

        for (PrometheusMetricSample sample : samples) {

            if (sample.value() == 1.0) {
                containsHealthyTarget = true;
                break;
            }
        }

        assertTrue(containsHealthyTarget);
    }
}
