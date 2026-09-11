package com.ananyapraneet.monitoring.incidentcontext.metrics;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PrometheusHttpClientTests {

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
    void shouldQueryPrometheusForHttpMetrics() throws Exception {
        String response = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": [
                      {
                        "metric": {},
                        "value": [1757516400, "0.08"]
                      }
                    ]
                  }
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(response)
        );

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(response)
        );

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpClient client = new PrometheusHttpClient(
                WebClient.builder(),
                baseUrl
        );

        Map<String, Object> metrics = client.queryMetrics("order-service");

        assertNotNull(metrics);
        assertEquals(2, metrics.size());
        assertEquals(
                metrics.get("http_5xx_rate").getClass(),
                metrics.get("request_rate").getClass()
        );

        var firstRequest = mockWebServer.takeRequest();
        var secondRequest = mockWebServer.takeRequest();

        assertEquals("/api/v1/query", firstRequest.getRequestUrl().encodedPath());
        assertEquals("/api/v1/query", secondRequest.getRequestUrl().encodedPath());

        String firstQuery = firstRequest.getRequestUrl()
                .queryParameter("query");

        String secondQuery = secondRequest.getRequestUrl()
                .queryParameter("query");

        assertNotNull(firstQuery);
        assertNotNull(secondQuery);

        assertEquals(
                "sum(rate(http_server_requests_seconds_count{job=\"order-service\",status=~\"5..\"}[5m]))",
                firstQuery
        );

        assertEquals(
                "sum(rate(http_server_requests_seconds_count{job=\"order-service\"}[5m]))",
                secondQuery
        );
    }

    @Test
    void shouldReturnEmptyMetricsWhenPrometheusIsUnavailable() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(503)
        );

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(503)
        );

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpClient client = new PrometheusHttpClient(
                WebClient.builder(),
                baseUrl
        );

        Map<String, Object> metrics = client.queryMetrics("order-service");

        assertNotNull(metrics);
        assertEquals(2, metrics.size());
        assertEquals(
                Map.of(),
                metrics.get("http_5xx_rate")
        );
        assertEquals(
                Map.of(),
                metrics.get("request_rate")
        );
    }
}
