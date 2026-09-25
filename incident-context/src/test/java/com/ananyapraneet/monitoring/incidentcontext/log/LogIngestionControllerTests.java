package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.store.InMemoryLogEvidenceStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LogIngestionControllerTests {

    private MockMvc mockMvc;

    private InMemoryLogEvidenceStore store;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        store = new InMemoryLogEvidenceStore();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new LogIngestionController(store)
                )
                .build();
    }

    @Test
    void shouldAcceptValidLogBatch() throws Exception {
        LogEvidence log = new LogEvidence(
                Instant.parse("2026-09-25T10:00:00Z"),
                "ERROR",
                "order-service",
                "request-123",
                "Database connection failed",
                "java.sql.SQLException"
        );

        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                List.of(log)
                                        )
                                )
                )
                .andExpect(status().isAccepted());

        assertEquals(
                1,
                store.getByService("order-service").size()
        );
    }

    @Test
    void shouldRejectLogBatchWithoutTimestamp() throws Exception {
        String json = """
                [
                  {
                    "timestamp": null,
                    "level": "ERROR",
                    "service": "order-service",
                    "requestId": "request-123",
                    "message": "Database connection failed",
                    "exception": "java.sql.SQLException"
                  }
                ]
                """;

        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLogBatchWithoutService() throws Exception {
        String json = """
                [
                  {
                    "timestamp": "2026-09-25T10:00:00Z",
                    "level": "ERROR",
                    "service": null,
                    "requestId": "request-123",
                    "message": "Database connection failed",
                    "exception": "java.sql.SQLException"
                  }
                ]
                """;

        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLogBatchWithoutMessage() throws Exception {
        String json = """
                [
                  {
                    "timestamp": "2026-09-25T10:00:00Z",
                    "level": "ERROR",
                    "service": "order-service",
                    "requestId": "request-123",
                    "message": null,
                    "exception": "java.sql.SQLException"
                  }
                ]
                """;

        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLogBatchWithoutLevel() throws Exception {
        String json = """
                [
                  {
                    "timestamp": "2026-09-25T10:00:00Z",
                    "level": null,
                    "service": "order-service",
                    "requestId": "request-123",
                    "message": "Database connection failed",
                    "exception": "java.sql.SQLException"
                  }
                ]
                """;

        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectEmptyLogBatch() throws Exception {
        mockMvc.perform(
                        post("/api/v1/logs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[]")
                )
                .andExpect(status().isBadRequest());
    }
}
