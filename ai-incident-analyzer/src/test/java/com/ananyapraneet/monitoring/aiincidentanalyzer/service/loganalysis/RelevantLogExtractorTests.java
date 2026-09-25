package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RelevantLogExtractorTests {

    private final ErrorClusterer errorClusterer =
            new ErrorClusterer();

    private final RelevantLogExtractor extractor =
            new RelevantLogExtractor(errorClusterer);

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void extractsLogsFromRelevantCluster() {

        NormalizedLog databaseLog =
                normalizedLog(
                        "order-service",
                        "Connection pool exhausted for order-db",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog unrelatedLog =
                normalizedLog(
                        "order-service",
                        "Scheduled cleanup completed",
                        ErrorPattern.UNKNOWN
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(
                                databaseLog,
                                unrelatedLog
                        ),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        "order-service"
                );

        assertEquals(1, result.size());

        RelevantLog relevantLog =
                result.get(0);

        assertEquals(
                databaseLog,
                relevantLog.log()
        );

        assertEquals(
                LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION,
                relevantLog.clusterType()
        );

        assertEquals(
                "Matches a log cluster relevant to the current incident",
                relevantLog.relevanceReason()
        );
    }

    @Test
    void extractsMultipleRelevantClusterTypes() {

        NormalizedLog databaseLog =
                normalizedLog(
                        "order-service",
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog httpLog =
                normalizedLog(
                        "order-service",
                        "HTTP 500 Internal Server Error",
                        ErrorPattern.HTTP_5XX
                );

        NormalizedLog timeoutLog =
                normalizedLog(
                        "order-service",
                        "Request timed out",
                        ErrorPattern.TIMEOUT
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(
                                databaseLog,
                                httpLog,
                                timeoutLog
                        ),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION,
                                LogClusterType
                                        .HTTP_SERVER_FAILURE
                        ),
                        "order-service"
                );

        assertEquals(2, result.size());

        assertEquals(
                databaseLog,
                result.get(0).log()
        );

        assertEquals(
                httpLog,
                result.get(1).log()
        );
    }

    @Test
    void ignoresLogsFromAnotherService() {

        NormalizedLog orderServiceLog =
                normalizedLog(
                        "order-service",
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog userServiceLog =
                normalizedLog(
                        "user-service",
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(
                                orderServiceLog,
                                userServiceLog
                        ),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        "order-service"
                );

        assertEquals(1, result.size());

        assertEquals(
                orderServiceLog,
                result.get(0).log()
        );
    }

    @Test
    void returnsEmptyWhenNoRequestedClusterMatches() {

        NormalizedLog timeoutLog =
                normalizedLog(
                        "order-service",
                        "Request timed out",
                        ErrorPattern.TIMEOUT
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(timeoutLog),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        "order-service"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForEmptyLogs() {

        List<RelevantLog> result =
                extractor.extract(
                        List.of(),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        "order-service"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenNoRelevantClustersProvided() {

        NormalizedLog databaseLog =
                normalizedLog(
                        "order-service",
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(databaseLog),
                        Set.of(),
                        "order-service"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void includesAllLogsFromRelevantCluster() {

        NormalizedLog first =
                normalizedLog(
                        "order-service",
                        "Connection pool exhausted for order-db",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog second =
                normalizedLog(
                        "order-service",
                        "Failed to acquire JDBC connection",
                        ErrorPattern.DATABASE_CONNECTION_FAILURE
                );

        NormalizedLog third =
                normalizedLog(
                        "order-service",
                        "Database connection timeout",
                        ErrorPattern.DATABASE_CONNECTION_TIMEOUT
                );

        List<RelevantLog> result =
                extractor.extract(
                        List.of(
                                first,
                                second,
                                third
                        ),
                        Set.of(
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        "order-service"
                );

        assertEquals(3, result.size());

        assertEquals(first, result.get(0).log());
        assertEquals(second, result.get(1).log());
        assertEquals(third, result.get(2).log());
    }

    private NormalizedLog normalizedLog(
            String service,
            String message,
            ErrorPattern errorPattern) {

        return new NormalizedLog(
                service,
                LogSeverity.ERROR,
                message,
                message.toLowerCase(),
                errorPattern,
                timestamp
        );
    }
}
