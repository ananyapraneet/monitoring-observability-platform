package com.ananyapraneet.monitoring.incidentcontext.health;

import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.Map;

@Service
public class HealthHttpClient implements HealthClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, String> serviceUrls;

    public HealthHttpClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${services.user-service-url}") String userServiceUrl,
            @Value("${services.order-service-url}") String orderServiceUrl) {

        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;

        this.serviceUrls = Map.of(
                "user-service", userServiceUrl,
                "order-service", orderServiceUrl
        );
    }

    @Override
    public HealthEvidence getHealth(String service) {

        String baseUrl = serviceUrls.get(service);

        if (baseUrl == null) {
            return new HealthEvidence(
                    "UNKNOWN",
                    Collections.emptyMap()
            );
        }

        String response = webClient.get()
                .uri(baseUrl + "/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response == null) {
            return new HealthEvidence(
                    "UNKNOWN",
                    Collections.emptyMap()
            );
        }

        try {
            Map<String, Object> healthResponse =
                    objectMapper.readValue(
                            response,
                            new TypeReference<>() {
                            }
                    );

            String status = String.valueOf(
                    healthResponse.getOrDefault("status", "UNKNOWN")
            );

            Map<String, Object> components =
                    healthResponse.get("components") instanceof Map
                            ? objectMapper.convertValue(
                                    healthResponse.get("components"),
                                    new TypeReference<>() {
                                    }
                            )
                            : Collections.emptyMap();

            return new HealthEvidence(
                    status,
                    components
            );

        } catch (Exception exception) {
            return new HealthEvidence(
                    "UNKNOWN",
                    Collections.emptyMap()
            );
        }
    }
}
