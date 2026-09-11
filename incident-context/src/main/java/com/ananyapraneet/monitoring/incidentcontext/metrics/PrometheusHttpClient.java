package com.ananyapraneet.monitoring.incidentcontext.metrics;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

@Service
public class PrometheusHttpClient implements PrometheusClient {

    private final WebClient webClient;
    private final String baseUrl;

    public PrometheusHttpClient(
            WebClient.Builder webClientBuilder,
            @Value("${prometheus.base-url}") String baseUrl) {

        this.webClient = webClientBuilder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public Map<String, Object> queryMetrics(String service) {

        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put(
                "http_5xx_rate",
                query(
                        "sum(rate(http_server_requests_seconds_count{job=\""
                                + service
                                + "\",status=~\"5..\"}[5m]))"
                )
        );

        metrics.put(
                "request_rate",
                query(
                        "sum(rate(http_server_requests_seconds_count{job=\""
                                + service
                                + "\"}[5m]))"
                )
        );

        return metrics;
    }

    private Object query(String promQl) {

        URI uri = UriComponentsBuilder
            .fromUriString(baseUrl)
            .path("/api/v1/query")
            .queryParam("query", promQl)
            .build()
            .encode()
            .toUri();

        try {
            return webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception exception) {
            return Collections.emptyMap();
        }
    }
}
