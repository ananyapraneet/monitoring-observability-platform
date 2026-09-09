package com.ananyapraneet.monitoring.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/orders")
public class OrderGatewayController {

    private final RestClient restClient;

    public OrderGatewayController(
            RestClient.Builder restClientBuilder,
            @Value("${services.order-service.url}") String orderServiceUrl) {

        this.restClient = restClientBuilder
                .baseUrl(orderServiceUrl)
                .build();
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody String body) {
        String response = restClient.post()
                .uri("/orders")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity
                .status(org.springframework.http.HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getOrder(@PathVariable Long id) {
        String response = restClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<String> getOrders() {
        String response = restClient.get()
                .uri("/orders")
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateOrder(
            @PathVariable Long id,
            @RequestBody String body) {

        String response = restClient.put()
                .uri("/orders/{id}", id)
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        restClient.delete()
                .uri("/orders/{id}", id)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.noContent().build();
    }
}
