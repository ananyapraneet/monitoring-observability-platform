package com.ananyapraneet.monitoring.aiincidentanalyzer.controller;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.IncidentContextClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AnalysisSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.IncidentAnalyzer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidentAnalysisControllerTests {

    private final IncidentContextClient incidentContextClient =
            mock(IncidentContextClient.class);

    private final IncidentAnalyzer incidentAnalyzer =
            mock(IncidentAnalyzer.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new IncidentAnalysisController(
                            incidentContextClient,
                            incidentAnalyzer))
            .build();

    @Test
    void shouldAnalyzeLatestIncidentContext() throws Exception {
        IncidentContext context = new IncidentContext(
                "APIErrorRateHigh",
                "HIGH",
                "order-service",
                List.of(),
                Map.of("http_5xx_rate", 0.08),
                List.of(),
                new HealthEvidence("UP", Map.of()),
                List.of(),
                List.of());

        IncidentAnalysis analysis = new IncidentAnalysis(
                "APIErrorRateHigh",
                AnalysisSeverity.HIGH,
                "order-service",
                "API degradation detected.",
                new Correlation(
                        "Server-side HTTP errors indicate API degradation.",
                        List.of("alert", "metrics", "http_errors")),
                List.of(
                        new Evidence(
                                "http_error",
                                "incident-context",
                                "HTTP 500 errors detected.")),
                "Server-side API failure",
                List.of("Inspect application errors and service health."),
                0.65);

        when(incidentContextClient.getLatestContext()).thenReturn(context);
        when(incidentAnalyzer.analyze(context)).thenReturn(analysis);

        mockMvc.perform(
                        post("/api/v1/analyze")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident").value("APIErrorRateHigh"))
                .andExpect(jsonPath("$.severity").value("HIGH"))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.summary")
                        .value("API degradation detected."))
                .andExpect(jsonPath("$.probableRootCause")
                        .value("Server-side API failure"))
                .andExpect(jsonPath("$.confidence").value(0.65));
    }

    @Test
    void shouldReturnCautiousAnalysisWhenContextIsMissing() throws Exception {
        IncidentAnalysis analysis = new IncidentAnalysis(
                "Unknown",
                AnalysisSeverity.UNKNOWN,
                "unknown",
                "Insufficient incident evidence for a specific diagnosis.",
                new Correlation(
                        "No meaningful signals were available for correlation.",
                        List.of()),
                List.of(),
                "Unknown - insufficient evidence",
                List.of(),
                0.10);

        when(incidentContextClient.getLatestContext()).thenReturn(null);
        when(incidentAnalyzer.analyze(null)).thenReturn(analysis);

        mockMvc.perform(post("/api/v1/analyze"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.severity").value("UNKNOWN"))
                .andExpect(jsonPath("$.probableRootCause")
                        .value("Unknown - insufficient evidence"))
                .andExpect(jsonPath("$.confidence").value(0.10));
    }
}
