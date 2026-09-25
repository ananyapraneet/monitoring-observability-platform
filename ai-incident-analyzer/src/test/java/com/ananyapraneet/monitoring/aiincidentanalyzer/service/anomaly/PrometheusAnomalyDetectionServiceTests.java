package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricSample;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus.PrometheusMetricsClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrometheusAnomalyDetectionServiceTests {

    private static final Instant END =
            Instant.parse("2026-09-25T02:00:00Z");

    @Test
    void detectsAnomalyFromPrometheusSamples() {

        PrometheusMetricsClient client =
                new FakePrometheusMetricsClient(
                        List.of(
                                new PrometheusMetricSample(
                                        80.0,
                                        END.minusSeconds(240)
                                ),
                                new PrometheusMetricSample(
                                        90.0,
                                        END.minusSeconds(180)
                                ),
                                new PrometheusMetricSample(
                                        100.0,
                                        END.minusSeconds(120)
                                ),
                                new PrometheusMetricSample(
                                        110.0,
                                        END.minusSeconds(60)
                                ),
                                new PrometheusMetricSample(
                                        120.0,
                                        END
                                )
                        ),
                        new PrometheusMetricSample(
                                450.0,
                                END
                        )
                );

        PrometheusAnomalyDetectionService service =
                new PrometheusAnomalyDetectionService(client);

        AnomalyResult result = service.detect(
                AnomalyMetricType.LATENCY,
                "order-service",
                "http_request_duration_ms",
                END,
                Duration.ofMinutes(5),
                60
        );

        assertTrue(result.anomalous());
        assertEquals(450.0, result.currentValue());
        assertEquals(100.0, result.baselineMean());
    }

    @Test
    void identifiesNormalPrometheusValue() {

        PrometheusMetricsClient client =
                new FakePrometheusMetricsClient(
                        List.of(
                                new PrometheusMetricSample(
                                        80.0,
                                        END.minusSeconds(240)
                                ),
                                new PrometheusMetricSample(
                                        90.0,
                                        END.minusSeconds(180)
                                ),
                                new PrometheusMetricSample(
                                        100.0,
                                        END.minusSeconds(120)
                                ),
                                new PrometheusMetricSample(
                                        110.0,
                                        END.minusSeconds(60)
                                )
                        ),
                        new PrometheusMetricSample(
                                105.0,
                                END
                        )
                );

        PrometheusAnomalyDetectionService service =
                new PrometheusAnomalyDetectionService(client);

        AnomalyResult result = service.detect(
                AnomalyMetricType.LATENCY,
                "order-service",
                "http_request_duration_ms",
                END,
                Duration.ofMinutes(5),
                60
        );

        assertTrue(!result.anomalous());
        assertEquals(105.0, result.currentValue());
    }

    @Test
    void rejectsInsufficientPrometheusSamples() {

        PrometheusMetricsClient client =
                new FakePrometheusMetricsClient(
                        List.of(
                                new PrometheusMetricSample(
                                        100.0,
                                        END
                                )
                        ),
                        new PrometheusMetricSample(
                                120.0,
                                END
                        )
                );

        PrometheusAnomalyDetectionService service =
                new PrometheusAnomalyDetectionService(client);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.detect(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        "http_request_duration_ms",
                        END,
                        Duration.ofMinutes(5),
                        60
                )
        );
    }

    private static class FakePrometheusMetricsClient
            implements PrometheusMetricsClient {

        private final List<PrometheusMetricSample> historicalSamples;
        private final PrometheusMetricSample currentSample;

        private FakePrometheusMetricsClient(
                List<PrometheusMetricSample> historicalSamples,
                PrometheusMetricSample currentSample) {

            this.historicalSamples = historicalSamples;
            this.currentSample = currentSample;
        }

        @Override
        public List<PrometheusMetricSample> queryRange(
                String query,
                Instant start,
                Instant end,
                long stepSeconds) {

            return historicalSamples;
        }

        @Override
        public PrometheusMetricSample queryInstant(
                String query,
                Instant timestamp) {

            return currentSample;
        }
    }
}
