package com.ananyapraneet.monitoring.incidentcontext.controller;

import com.ananyapraneet.monitoring.incidentcontext.health.HealthClient;
import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;
import com.ananyapraneet.monitoring.incidentcontext.metrics.PrometheusClient;
import com.ananyapraneet.monitoring.incidentcontext.log.LogClient;
import com.ananyapraneet.monitoring.incidentcontext.http.HttpErrorClient;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AlertmanagerWebhookControllerTests {

    private MockMvc mockMvc;

    @Autowired
    void setUp(WebApplicationContext webApplicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    void shouldBuildIncidentContextFromAlertmanagerWebhook() throws Exception {

        String payload = """
                {
                  "receiver": "default",
                  "status": "firing",
                  "groupKey": "{}:{alertname=\\"HighHttp5xxErrorRate\\"}",
                  "truncatedAlerts": "0",
                  "alerts": [
                    {
                      "status": "firing",
                      "labels": {
                        "alertname": "HighHttp5xxErrorRate",
                        "severity": "warning",
                        "service": "order-service",
                        "environment": "local",
                        "instance": "order-service:8081"
                      },
                      "annotations": {
                        "summary": "High HTTP 5xx error rate",
                        "description": "HTTP 5xx error rate exceeded 5%",
                        "runbook": "Check order-service logs and database health"
                      },
                      "startsAt": "2026-09-10T15:00:00Z",
                      "endsAt": "0001-01-01T00:00:00Z",
                      "generatorURL": "http://prometheus:9090/graph",
                      "fingerprint": "abc123"
                    }
                  ]
                }
                """;

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident")
                        .value("HighHttp5xxErrorRate"))
                .andExpect(jsonPath("$.severity")
                        .value("warning"))
                .andExpect(jsonPath("$.service")
                        .value("order-service"))
                .andExpect(jsonPath("$.alerts")
                        .isArray())
                .andExpect(jsonPath("$.alerts.length()")
                        .value(1))
                .andExpect(jsonPath("$.alerts[0].alertName")
                        .value("HighHttp5xxErrorRate"))
                .andExpect(jsonPath("$.alerts[0].status")
                        .value("firing"))
                .andExpect(jsonPath("$.alerts[0].severity")
                        .value("warning"))
                .andExpect(jsonPath("$.alerts[0].service")
                        .value("order-service"))
                .andExpect(jsonPath("$.alerts[0].instance")
                        .value("order-service:8081"))
                .andExpect(jsonPath("$.alerts[0].summary")
                        .value("High HTTP 5xx error rate"))
                .andExpect(jsonPath("$.alerts[0].description")
                        .value("HTTP 5xx error rate exceeded 5%"))
                .andExpect(jsonPath("$.alerts[0].runbook")
                        .value("Check order-service logs and database health"))
                .andExpect(jsonPath("$.metrics")
                        .isMap())
                .andExpect(jsonPath("$.metrics.http_5xx_rate")
                        .value(0.08))
                .andExpect(jsonPath("$.metrics.request_rate")
                        .value(125.0))
                .andExpect(jsonPath("$.logs")
                        .isArray())
                .andExpect(jsonPath("$.logs.length()")
                        .value(0))
                .andExpect(jsonPath("$.health.status")
                        .value("UP"))
                .andExpect(jsonPath("$.httpErrors")
                        .isArray())
                .andExpect(jsonPath("$.httpErrors.length()")
                        .value(0))
                .andExpect(jsonPath("$.timeline")
                        .isArray())
                .andExpect(jsonPath("$.timeline.length()")
                        .value(1))
                .andExpect(jsonPath("$.timeline[0].type")
                        .value("ALERT_FIRING"));
    }

    @TestConfiguration
    static class TestPrometheusConfiguration {

        @Bean
        PrometheusClient prometheusClient() {
            return service -> Map.of(
                    "http_5xx_rate", 0.08,
                    "request_rate", 125.0
            );
        }

        @Bean
        HealthClient healthClient() {
            return service -> new HealthEvidence(
                    "UP",
                    Map.of()
            );
        }

        @Bean
        HttpErrorClient httpErrorClient() {
            return service -> List.of();
        }

        @Bean
        LogClient logClient() {
            return service -> List.of();
        }

    }
}
