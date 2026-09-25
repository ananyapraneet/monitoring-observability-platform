package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentLogSummarizerTests {

    private final ErrorClusterer errorClusterer =
            new ErrorClusterer();

    private final IncidentLogRelevanceAnalyzer relevanceAnalyzer =
            new IncidentLogRelevanceAnalyzer();

    private final LogEvidenceNormalizer logEvidenceNormalizer =
            new LogEvidenceNormalizer(
                    new ErrorPatternNormalizer()
            );

    private final RelevantLogExtractor relevantLogExtractor =
            new RelevantLogExtractor(
                    errorClusterer
            );

    private final IncidentRelevantLogExtractor incidentExtractor =
            new IncidentRelevantLogExtractor(
                    relevanceAnalyzer,
                    logEvidenceNormalizer,
                    relevantLogExtractor
            );

    private final IncidentLogSummarizer summarizer =
            new IncidentLogSummarizer(
                    incidentExtractor,
                    new LogSummaryGenerator()
            );

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void summarizesDatabaseIncidentLogs() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS
                                )
                        ),
                        List.of(
                                logEvidence(
                                        "Connection pool exhausted"
                                ),
                                logEvidence(
                                        "Failed to acquire JDBC connection"
                                ),
                                logEvidence(
                                        "Database connection timeout"
                                )
                        )
                );

        List<LogSummary> result =
                summarizer.summarize(context);

        assertEquals(1, result.size());

        LogSummary summary =
                result.get(0);

        assertEquals(
                LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION,
                summary.clusterType()
        );

        assertEquals(
                3,
                summary.occurrenceCount()
        );

        assertEquals(
                "Database connection exhaustion errors were observed 3 times in Order Service.",
                summary.summary()
        );
    }

    @Test
    void summarizesHttpServerErrors() {

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(
                                logEvidence(
                                        "HTTP 500 Internal Server Error"
                                ),
                                logEvidence(
                                        "HTTP 502 Bad Gateway"
                                )
                        )
                );

        context =
                new IncidentContext(
                        context.incident(),
                        context.severity(),
                        context.service(),
                        context.alerts(),
                        context.metrics(),
                        context.logs(),
                        context.health(),
                        List.of(
                                new com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence(
                                        timestamp,
                                        "GET",
                                        "/orders",
                                        500,
                                        "order-service",
                                        "request-123",
                                        "HTTP error"
                                )
                        ),
                        context.timeline(),
                        context.anomalies()
                );

        List<LogSummary> result =
                summarizer.summarize(context);

        assertEquals(1, result.size());

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                result.get(0).clusterType()
        );

        assertEquals(
                2,
                result.get(0).occurrenceCount()
        );
    }

    @Test
    void returnsEmptyWhenNoRelevantLogsExist() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS
                                )
                        ),
                        List.of()
                );

        List<LogSummary> result =
                summarizer.summarize(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenIncidentHasNoRelevantSignals() {

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(
                                logEvidence(
                                        "Connection pool exhausted"
                                )
                        )
                );

        List<LogSummary> result =
                summarizer.summarize(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullContext() {

        List<LogSummary> result =
                summarizer.summarize(null);

        assertTrue(result.isEmpty());
    }

    private IncidentContext incidentContext(
            List<AnomalyEvidence> anomalies,
            List<LogEvidence> logs) {

        return new IncidentContext(
                "Order service incident",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(),
                logs,
                null,
                List.of(),
                List.of(),
                anomalies
        );
    }

    private LogEvidence logEvidence(
            String message) {

        return new LogEvidence(
                timestamp,
                "ERROR",
                "order-service",
                "request-123",
                message,
                null
        );
    }

    private AnomalyEvidence anomaly(
            AnomalyMetricType metricType) {

        return new AnomalyEvidence(
                metricType,
                "order-service",
                100.0,
                50.0,
                10.0,
                5.0,
                true,
                timestamp
        );
    }
}
