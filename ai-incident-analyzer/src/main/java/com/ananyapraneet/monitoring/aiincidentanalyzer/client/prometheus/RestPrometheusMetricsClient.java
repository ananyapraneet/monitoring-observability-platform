package com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RestPrometheusMetricsClient
        implements PrometheusMetricsClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public RestPrometheusMetricsClient(
            String prometheusBaseUrl,
            ObjectMapper objectMapper) {

        this(
                RestClient.builder(),
                prometheusBaseUrl,
                objectMapper
        );
    }

    public RestPrometheusMetricsClient(
            RestClient.Builder restClientBuilder,
            String prometheusBaseUrl,
            ObjectMapper objectMapper) {

        this.restClient = restClientBuilder
                .baseUrl(prometheusBaseUrl)
                .build();

        this.objectMapper = objectMapper;
    }

    @Override
    public List<PrometheusMetricSample> queryRange(
            String query,
            Instant start,
            Instant end,
            long stepSeconds) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/query_range")
                        .queryParam("query", query)
                        .queryParam(
                                "start",
                                start.getEpochSecond())
                        .queryParam(
                                "end",
                                end.getEpochSecond())
                        .queryParam("step", stepSeconds)
                        .build())
                .retrieve()
                .body(String.class);

        return parseMatrixResponse(response);
    }

    @Override
    public PrometheusMetricSample queryInstant(
            String query,
            Instant timestamp) {

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/query")
                        .queryParam("query", query)
                        .queryParam(
                                "time",
                                timestamp.getEpochSecond())
                        .build())
                .retrieve()
                .body(String.class);

        return parseVectorResponse(response);
    }

    private List<PrometheusMetricSample> parseMatrixResponse(
            String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            validateResponse(root);

            JsonNode results = root
                    .path("data")
                    .path("result");

            List<PrometheusMetricSample> samples =
                    new ArrayList<>();

            for (JsonNode result : results) {

                JsonNode values = result.path("values");

                for (JsonNode value : values) {

                    long timestamp =
                            value.get(0).asLong();

                    String metricValueText =
                            value.get(1).asText();

                    if (isNonFiniteValue(metricValueText)) {
                        continue;
                    }

                    double metricValue =
                            Double.parseDouble(metricValueText);

                    samples.add(
                            new PrometheusMetricSample(
                                    metricValue,
                                    Instant.ofEpochSecond(
                                            timestamp)
                            )
                    );
                }
            }

            return samples;

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to parse Prometheus range response",
                    exception
            );
        }
    }

    private PrometheusMetricSample parseVectorResponse(
            String response) {

        try {
            JsonNode root = objectMapper.readTree(response);

            validateResponse(root);

            JsonNode results = root
                    .path("data")
                    .path("result");

            if (!results.isArray() || results.isEmpty()) {
                throw new IllegalStateException(
                        "Prometheus returned no instant result");
            }

            JsonNode value = results
                    .get(0)
                    .path("value");

            long timestamp =
                    value.get(0).asLong();

            String metricValueText =
                    value.get(1).asText();

            if (isNonFiniteValue(metricValueText)) {
                throw new IllegalStateException(
                        "Prometheus returned a non-finite instant value");
            }

            double metricValue =
                    Double.parseDouble(metricValueText);

            return new PrometheusMetricSample(
                    metricValue,
                    Instant.ofEpochSecond(timestamp)
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to parse Prometheus instant response",
                    exception
            );
        }
    }

    private boolean isNonFiniteValue(String value) {

        return "NaN".equals(value)
                || "+Inf".equals(value)
                || "-Inf".equals(value);
    }

    private void validateResponse(JsonNode root) {

        String status = root
                .path("status")
                .asText();

        if (!"success".equals(status)) {
            String error = root
                    .path("error")
                    .asText("Unknown Prometheus error");

            throw new IllegalStateException(
                    "Prometheus query failed: " + error
            );
        }
    }
}
