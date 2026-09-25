package com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import java.net.InetSocketAddress;

import java.nio.charset.StandardCharsets;

import java.time.Instant;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestPrometheusMetricsClientTests {

    private HttpServer server;

    private RestPrometheusMetricsClient client;

    @BeforeEach
    void setUp() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress(0),
                0
        );

        client = new RestPrometheusMetricsClient(
                "http://localhost:" + server.getAddress().getPort(),
                new ObjectMapper()
        );

        server.start();
    }

    @AfterEach
    void tearDown() {

        server.stop(0);
    }

    @Test
    void parsesRangeQueryResponse() {

        server.createContext(
                "/api/v1/query_range",
                exchange -> {

                    String response = """
                            {
                              "status": "success",
                              "data": {
                                "resultType": "matrix",
                                "result": [
                                  {
                                    "metric": {
                                      "__name__": "http_requests_total"
                                    },
                                    "values": [
                                      [1727200800, "100"],
                                      [1727200860, "110"],
                                      [1727200920, "120"]
                                    ]
                                  }
                                ]
                              }
                            }
                            """;

                    sendResponse(exchange, response);
                }
        );

        List<PrometheusMetricSample> samples =
                client.queryRange(
                        "http_requests_total",
                        Instant.ofEpochSecond(1727200800),
                        Instant.ofEpochSecond(1727200920),
                        60
                );

        assertEquals(3, samples.size());

        assertEquals(
                100.0,
                samples.get(0).value()
        );

        assertEquals(
                Instant.ofEpochSecond(1727200800),
                samples.get(0).timestamp()
        );

        assertEquals(
                110.0,
                samples.get(1).value()
        );

        assertEquals(
                120.0,
                samples.get(2).value()
        );
    }

    @Test
    void parsesInstantQueryResponse() {

        server.createContext(
                "/api/v1/query",
                exchange -> {

                    String response = """
                            {
                              "status": "success",
                              "data": {
                                "resultType": "vector",
                                "result": [
                                  {
                                    "metric": {
                                      "__name__": "http_requests_total"
                                    },
                                    "value": [
                                      1727200920,
                                      "125"
                                    ]
                                  }
                                ]
                              }
                            }
                            """;

                    sendResponse(exchange, response);
                }
        );

        PrometheusMetricSample sample =
                client.queryInstant(
                        "http_requests_total",
                        Instant.ofEpochSecond(1727200920)
                );

        assertEquals(
                125.0,
                sample.value()
        );

        assertEquals(
                Instant.ofEpochSecond(1727200920),
                sample.timestamp()
        );
    }

    @Test
    void rejectsPrometheusErrorResponse() {

        server.createContext(
                "/api/v1/query",
                exchange -> {

                    String response = """
                            {
                              "status": "error",
                              "errorType": "bad_data",
                              "error": "invalid query"
                            }
                            """;

                    sendResponse(exchange, response);
                }
        );

        assertThrows(
                IllegalStateException.class,
                () -> client.queryInstant(
                        "invalid-query",
                        Instant.ofEpochSecond(1727200920)
                )
        );
    }

    @Test
    void rejectsEmptyInstantResult() {

        server.createContext(
                "/api/v1/query",
                exchange -> {

                    String response = """
                            {
                              "status": "success",
                              "data": {
                                "resultType": "vector",
                                "result": []
                              }
                            }
                            """;

                    sendResponse(exchange, response);
                }
        );

        assertThrows(
                IllegalStateException.class,
                () -> client.queryInstant(
                        "missing_metric",
                        Instant.ofEpochSecond(1727200920)
                )
        );
    }

    @Test
    void skipsNonFiniteValuesFromRangeResponse() {

        server.createContext(
                "/api/v1/query_range",
                exchange -> {

                    String response = """
                            {
                              "status": "success",
                              "data": {
                                "resultType": "matrix",
                                "result": [
                                  {
                                    "metric": {
                                      "job": "order-service"
                                    },
                                    "values": [
                                      [1000, "0.10"],
                                      [1060, "NaN"],
                                      [1120, "0.20"],
                                      [1180, "+Inf"],
                                      [1240, "0.30"]
                                    ]
                                  }
                                ]
                              }
                            }
                            """;

                    sendResponse(exchange, response);
                }
        );

        List<PrometheusMetricSample> samples =
                client.queryRange(
                        "test_query",
                        Instant.ofEpochSecond(1000),
                        Instant.ofEpochSecond(1240),
                        60
                );

        assertEquals(3, samples.size());

        assertEquals(
                0.10,
                samples.get(0).value()
        );

        assertEquals(
                0.20,
                samples.get(1).value()
        );

        assertEquals(
                0.30,
                samples.get(2).value()
        );
    }

    @Test
    void rejectsNonFiniteInstantValue() {

        String response = """
                {
                  "status": "success",
                  "data": {
                    "resultType": "vector",
                    "result": [
                      {
                        "metric": {
                          "job": "order-service"
                        },
                        "value": [1000, "NaN"]
                      }
                    ]
                  }
                }
                """;

        server.createContext(
                "/api/v1/query",
                exchange -> {

                    sendResponse(exchange, response);
                }
        );

        assertThrows(
                IllegalStateException.class,
                () -> client.queryInstant(
                        "test_query",
                        Instant.ofEpochSecond(1000)
                )
        );
    }

    private void sendResponse(
            HttpExchange exchange,
            String response) throws IOException {

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json"
        );

        exchange.sendResponseHeaders(
                200,
                responseBytes.length
        );

        try (OutputStream outputStream =
                     exchange.getResponseBody()) {

            outputStream.write(responseBytes);
        }
    }
}
