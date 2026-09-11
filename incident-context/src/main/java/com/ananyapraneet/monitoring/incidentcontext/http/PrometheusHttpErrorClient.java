package com.ananyapraneet.monitoring.incidentcontext.http;

import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class PrometheusHttpErrorClient implements HttpErrorClient {

    private final WebClient webClient;
    private final String baseUrl;

    public PrometheusHttpErrorClient(
            WebClient.Builder webClientBuilder,
            @Value("${prometheus.base-url}") String baseUrl) {

        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<HttpErrorEvidence> getHttpErrors(String service) {

        if (service == null || service.isBlank()) {
            return List.of();
        }

        String promQl =
                "http_server_requests_seconds_count{job=\""
                        + service
                        + "\",status=~\"4..|5..\"}";

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/api/v1/query")
                .queryParam("query", promQl)
                .build()
                .encode()
                .toUri();

        Map<String, Object> response;

        try {
            response = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(
                            new ParameterizedTypeReference<Map<String, Object>>() {}
                    )
                    .block();
        } catch (Exception exception) {
            return List.of();
        }

        return parseResponse(response, service);
    }

    private List<HttpErrorEvidence> parseResponse(
            Map<String, Object> response,
            String service) {

        if (response == null) {
            return List.of();
        }

        Object dataObject = response.get("data");

        if (!(dataObject instanceof Map<?, ?> data)) {
            return List.of();
        }

        Object resultObject = data.get("result");

        if (!(resultObject instanceof List<?> results)) {
            return List.of();
        }

        List<HttpErrorEvidence> errors = new ArrayList<>();

        for (Object resultObjectItem : results) {

            if (!(resultObjectItem instanceof Map<?, ?> result)) {
                continue;
            }

            Object metricObject = result.get("metric");
            Object valueObject = result.get("value");

            if (!(metricObject instanceof Map<?, ?> metric)
                    || !(valueObject instanceof List<?> value)
                    || value.size() < 2) {
                continue;
            }

            String method = stringValue(metric.get("method"));
            String endpoint = stringValue(metric.get("uri"));
            String statusValue = stringValue(metric.get("status"));

            if (method == null || endpoint == null || statusValue == null) {
                continue;
            }

            int status;

            try {
                status = Integer.parseInt(statusValue);
            } catch (NumberFormatException exception) {
                continue;
            }

            String timestampValue = stringValue(value.get(0));

            if (timestampValue == null) {
                continue;
            }

            try {
                double timestampSeconds =
                        Double.parseDouble(timestampValue);

                errors.add(
                        new HttpErrorEvidence(
                                Instant.ofEpochMilli(
                                        (long) (timestampSeconds * 1000)
                                ),
                                method,
                                endpoint,
                                status,
                                service,
                                null,
                                null
                        )
                );

            } catch (NumberFormatException exception) {
                // Ignore malformed Prometheus samples.
            }
        }

        return Collections.unmodifiableList(errors);
    }

    private String stringValue(Object value) {

        if (value == null) {
            return null;
        }

        return String.valueOf(value);
    }
}
