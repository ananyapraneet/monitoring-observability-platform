package com.ananyapraneet.monitoring.aiincidentanalyzer.client;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RestIncidentContextClient implements IncidentContextClient {

    private final RestClient restClient;

    public RestIncidentContextClient(
            RestClient.Builder restClientBuilder,
            @Value("${incident-context.base-url}") String baseUrl) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public IncidentContext getLatestContext() {
        return restClient.get()
                .uri("/api/v1/context/latest")
                .retrieve()
                .body(IncidentContext.class);
    }
}
