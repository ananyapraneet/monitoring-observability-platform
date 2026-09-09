package com.ananyapraneet.monitoring.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/users")
public class UserGatewayController {

    private final RestClient restClient;

    public UserGatewayController(
            RestClient.Builder restClientBuilder,
            @Value("${services.user-service.url}") String userServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(userServiceUrl)
                .build();
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody String body) {
        String response = restClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getUser(@PathVariable Long id) {
        String response = restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<String> getUsers() {
        String response = restClient.get()
                .uri("/users")
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        restClient.delete()
                .uri("/users/{id}", id)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.noContent().build();
    }
}
