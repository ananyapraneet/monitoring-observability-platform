package com.ananyapraneet.monitoring.incidentcontext.http;

import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PrometheusHttpErrorClientTests {

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
    void shouldParseHttpErrorMetrics() throws Exception {

        String response = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": [
                      {
                        "metric": {
                          "__name__": "http_server_requests_seconds_count",
                          "error": "none",
                          "exception": "none",
                          "instance": "order-service:8081",
                          "job": "order-service",
                          "method": "GET",
                          "outcome": "CLIENT_ERROR",
                          "status": "404",
                          "uri": "/orders/{id}"
                        },
                        "value": [
                          1789070505.889,
                          "1"
                        ]
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

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpErrorClient client =
                new PrometheusHttpErrorClient(
                        WebClient.builder(),
                        baseUrl
                );

        List<HttpErrorEvidence> errors =
                client.getHttpErrors("order-service");

        assertEquals(1, errors.size());

        HttpErrorEvidence error = errors.get(0);

        assertEquals(
                "GET",
                error.method()
        );

        assertEquals(
                "/orders/{id}",
                error.endpoint()
        );

        assertEquals(
                404,
                error.status()
        );

        assertEquals(
                "order-service",
                error.service()
        );

        assertNull(error.requestId());
        assertNull(error.message());

        var request = mockWebServer.takeRequest();

        assertEquals(
                "/api/v1/query",
                request.getRequestUrl().encodedPath()
        );

        assertEquals(
                "http_server_requests_seconds_count{job=\"order-service\",status=~\"4..|5..\"}",
                request.getRequestUrl().queryParameter("query")
        );
    }

    @Test
    void shouldReturnEmptyListWhenPrometheusHasNoErrors() {

        String response = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": []
                  }
                }
                """;

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setHeader("Content-Type", "application/json")
                        .setBody(response)
        );

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpErrorClient client =
                new PrometheusHttpErrorClient(
                        WebClient.builder(),
                        baseUrl
                );

        List<HttpErrorEvidence> errors =
                client.getHttpErrors("order-service");

        assertEquals(0, errors.size());
    }

    @Test
    void shouldReturnEmptyListForBlankService() throws Exception {

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpErrorClient client =
                new PrometheusHttpErrorClient(
                        WebClient.builder(),
                        baseUrl
                );

        List<HttpErrorEvidence> errors =
                client.getHttpErrors(" ");

        assertEquals(0, errors.size());

        assertEquals(
                0,
                mockWebServer.getRequestCount()
        );
    }

    @Test
    void shouldReturnEmptyListWhenPrometheusIsUnavailable() {

        mockWebServer.enqueue(
                new MockResponse()
                        .setResponseCode(503)
        );

        String baseUrl = mockWebServer.url("").toString();

        PrometheusHttpErrorClient client =
                new PrometheusHttpErrorClient(
                        WebClient.builder(),
                        baseUrl
                );

        List<HttpErrorEvidence> errors =
                client.getHttpErrors("order-service");

        assertEquals(0, errors.size());
    }

}
