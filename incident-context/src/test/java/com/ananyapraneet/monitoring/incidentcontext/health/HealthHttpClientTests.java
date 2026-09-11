package com.ananyapraneet.monitoring.incidentcontext.health;

import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HealthHttpClientTests {

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldQueryServiceHealthEndpoint() throws Exception {

        String response = """
                {
                  "status": "UP",
                  "groups": [
                    "liveness",
                    "readiness"
                  ]
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(response)
        );

        String baseUrl = mockWebServer.url("").toString();

        HealthHttpClient client = new HealthHttpClient(
                WebClient.builder(),
                new ObjectMapper(),
                baseUrl,
                baseUrl
        );

        HealthEvidence health = client.getHealth("order-service");

        assertNotNull(health);
        assertEquals("UP", health.status());
        assertNotNull(health.components());
        assertEquals(0, health.components().size());

        var request = mockWebServer.takeRequest();

        assertEquals(
                "/actuator/health",
                request.getRequestUrl().encodedPath()
        );
    }

    @Test
    void shouldReturnUnknownForUnsupportedService() {

        String baseUrl = mockWebServer.url("").toString();

        HealthHttpClient client = new HealthHttpClient(
                WebClient.builder(),
                new ObjectMapper(),
                baseUrl,
                baseUrl
        );

        HealthEvidence health = client.getHealth("unknown-service");

        assertNotNull(health);
        assertEquals("UNKNOWN", health.status());
        assertNotNull(health.components());
        assertEquals(0, health.components().size());
    }
}
