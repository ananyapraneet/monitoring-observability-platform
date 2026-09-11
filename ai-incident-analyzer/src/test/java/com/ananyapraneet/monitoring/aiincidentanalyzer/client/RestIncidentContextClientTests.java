package com.ananyapraneet.monitoring.aiincidentanalyzer.client;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RestIncidentContextClientTests {

    private HttpServer server;
    private RestIncidentContextClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();

        String baseUrl = "http://localhost:" + server.getAddress().getPort();

        client = new RestIncidentContextClient(
                RestClient.builder(),
                baseUrl
        );
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void shouldGetLatestIncidentContext() {
        server.createContext("/api/v1/context/latest", exchange -> {
            String response = """
                    {
                      "incident": "APIErrorRateHigh",
                      "severity": "HIGH",
                      "service": "order-service",
                      "alerts": [],
                      "metrics": {
                        "request_rate": 12.5
                      },
                      "logs": [],
                      "health": {
                        "status": "UP",
                        "components": {}
                      },
                      "httpErrors": [],
                      "timeline": []
                    }
                    """;

            sendResponse(exchange, 200, response);
        });

        IncidentContext context = client.getLatestContext();

        assertNotNull(context);
        assertEquals("APIErrorRateHigh", context.incident());
        assertEquals("HIGH", context.severity());
        assertEquals("order-service", context.service());
        assertEquals(12.5, context.metrics().get("request_rate"));
    }

    @Test
    void shouldReturnNullWhenNoIncidentContextExists() {
        server.createContext("/api/v1/context/latest", exchange ->
                sendResponse(exchange, 204, "")
        );

        IncidentContext context = client.getLatestContext();

        assertNull(context);
    }

    private void sendResponse(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        byte[] body = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, body.length);

        try (var outputStream = exchange.getResponseBody()) {
            outputStream.write(body);
        }
    }
}
