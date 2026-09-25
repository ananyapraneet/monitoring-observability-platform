package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentRelevantLogExtractorTests {

    private final ErrorClusterer errorClusterer =
            new ErrorClusterer();

    private final IncidentLogRelevanceAnalyzer relevanceAnalyzer =
            new IncidentLogRelevanceAnalyzer();

    private final LogEvidenceNormalizer logEvidenceNormalizer =
            new LogEvidenceNormalizer(
                    new ErrorPatternNormalizer()
            );

    private final RelevantLogExtractor logExtractor =
            new RelevantLogExtractor(errorClusterer);

    private final IncidentRelevantLogExtractor extractor =
            new IncidentRelevantLogExtractor(
                    relevanceAnalyzer,
                    logEvidenceNormalizer,
                    logExtractor
            );

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void extractsLogsRelevantToDatabaseIncident() {

        LogEvidence databaseLog =
                logEvidence(
                        "Connection pool exhausted for order-db"
                );

        LogEvidence connectionFailure =
                logEvidence(
                        "Failed to acquire JDBC connection"
                );

        LogEvidence unrelatedLog =
                logEvidence(
                        "Scheduled cleanup completed"
                );

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of(),
                        List.of(
                                databaseLog,
                                connectionFailure,
                                unrelatedLog
                        )
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertEquals(2, result.size());

        assertEquals(
                "Connection pool exhausted for order-db",
                result.get(0).log().originalMessage()
        );

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result.get(0).log().errorPattern()
        );

        assertEquals(
                "Failed to acquire JDBC connection",
                result.get(1).log().originalMessage()
        );

        assertEquals(
                ErrorPattern.DATABASE_CONNECTION_FAILURE,
                result.get(1).log().errorPattern()
        );

        assertEquals(
                LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION,
                result.get(0).clusterType()
        );

        assertEquals(
                LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION,
                result.get(1).clusterType()
        );
    }

    @Test
    void extractsLogsRelevantToHttpServerIncident() {

        LogEvidence http500 =
                logEvidence(
                        "HTTP 500 Internal Server Error"
                );

        LogEvidence http502 =
                logEvidence(
                        "HTTP 502 Bad Gateway"
                );

        LogEvidence timeout =
                logEvidence(
                        "Request timed out"
                );

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(
                                httpError(500)
                        ),
                        List.of(
                                http500,
                                http502,
                                timeout
                        )
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertEquals(2, result.size());

        assertEquals(
                "HTTP 500 Internal Server Error",
                result.get(0).log().originalMessage()
        );

        assertEquals(
                "HTTP 502 Bad Gateway",
                result.get(1).log().originalMessage()
        );

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                result.get(0).clusterType()
        );

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                result.get(1).clusterType()
        );
    }

    @Test
    void combinesDatabaseAndHttpRelevance() {

        LogEvidence databaseLog =
                logEvidence(
                        "Connection pool exhausted"
                );

        LogEvidence httpLog =
                logEvidence(
                        "HTTP 500 Internal Server Error"
                );

        LogEvidence timeoutLog =
                logEvidence(
                        "Request timed out"
                );

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of(
                                httpError(500)
                        ),
                        List.of(
                                databaseLog,
                                httpLog,
                                timeoutLog
                        )
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertEquals(2, result.size());

        assertEquals(
                "Connection pool exhausted",
                result.get(0).log().originalMessage()
        );

        assertEquals(
                "HTTP 500 Internal Server Error",
                result.get(1).log().originalMessage()
        );
    }

    @Test
    void ignoresLogsFromAnotherService() {

        LogEvidence orderLog =
                logEvidence(
                        "Connection pool exhausted"
                );

        LogEvidence userLog =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "user-service",
                        "request-456",
                        "Connection pool exhausted",
                        null
                );

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of(),
                        List.of(
                                orderLog,
                                userLog
                        )
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertEquals(1, result.size());

        assertEquals(
                "order-service",
                result.get(0).log().service()
        );
    }

    @Test
    void returnsEmptyWhenIncidentHasNoRelevantSignals() {

        LogEvidence databaseLog =
                logEvidence(
                        "Connection pool exhausted"
                );

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(),
                        List.of(databaseLog)
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenIncidentHasRelevantSignalButNoLogs() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of(),
                        List.of()
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullContext() {

        List<RelevantLog> result =
                extractor.extract(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void normalizesExceptionAlongWithLogMessage() {

        LogEvidence log =
                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "request-123",
                        "Failed to acquire JDBC connection",
                        "SQLTransientConnectionException: connection timed out"
                );

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of(),
                        List.of(log)
                );

        List<RelevantLog> result =
                extractor.extract(context);

        assertEquals(1, result.size());

        assertEquals(
                ErrorPattern.DATABASE_CONNECTION_TIMEOUT,
                result.get(0).log().errorPattern()
        );

        assertTrue(
                result.get(0).log().originalMessage().contains(
                        "SQLTransientConnectionException"
                )
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
            AnomalyMetricType metricType,
            boolean anomalous) {

        return new AnomalyEvidence(
                metricType,
                "order-service",
                100.0,
                50.0,
                10.0,
                anomalous ? 5.0 : 1.0,
                anomalous,
                timestamp
        );
    }

    private HttpErrorEvidence httpError(
            int status) {

        return new HttpErrorEvidence(
                timestamp,
                "GET",
                "/orders",
                status,
                "order-service",
                "request-123",
                "HTTP error"
        );
    }

    private IncidentContext incidentContext(
            List<AnomalyEvidence> anomalies,
            List<HttpErrorEvidence> httpErrors,
            List<LogEvidence> logs) {

        return new IncidentContext(
                "Order service incident",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(),
                logs,
                null,
                httpErrors,
                List.of(),
                anomalies
        );
    }
}
