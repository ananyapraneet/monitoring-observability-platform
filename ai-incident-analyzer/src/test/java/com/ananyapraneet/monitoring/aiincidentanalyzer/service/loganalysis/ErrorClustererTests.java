package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorCluster;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class ErrorClustererTests {

    private final ErrorClusterer clusterer =
            new ErrorClusterer();

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void groupsDatabaseConnectionErrorsIntoOneCluster() {

        NormalizedLog poolExhausted =
                normalizedLog(
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog connectionFailure =
                normalizedLog(
                        "Failed to acquire JDBC connection",
                        ErrorPattern.DATABASE_CONNECTION_FAILURE
                );

        NormalizedLog connectionTimeout =
                normalizedLog(
                        "Database connection timeout",
                        ErrorPattern.DATABASE_CONNECTION_TIMEOUT
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(
                        List.of(
                                poolExhausted,
                                connectionFailure,
                                connectionTimeout
                        )
                );

        assertEquals(1, clusters.size());

        ErrorCluster cluster = clusters.get(0);

        assertEquals(
                LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                cluster.clusterType()
        );

        assertEquals(
                3,
                cluster.occurrenceCount()
        );

        assertEquals(
                3,
                cluster.errorPatterns().size()
        );

        assertTrue(
                cluster.errorPatterns().contains(
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                )
        );

        assertTrue(
                cluster.errorPatterns().contains(
                        ErrorPattern.DATABASE_CONNECTION_FAILURE
                )
        );

        assertTrue(
                cluster.errorPatterns().contains(
                        ErrorPattern.DATABASE_CONNECTION_TIMEOUT
                )
        );

        assertEquals(
                3,
                cluster.logs().size()
        );
    }

    @Test
    void groupsHttpServerErrorsIntoHttpServerFailure() {

        NormalizedLog first =
                normalizedLog(
                        "HTTP 500 Internal Server Error",
                        ErrorPattern.HTTP_5XX
                );

        NormalizedLog second =
                normalizedLog(
                        "HTTP 500 while processing order",
                        ErrorPattern.HTTP_5XX
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(
                        List.of(first, second)
                );

        assertEquals(1, clusters.size());

        ErrorCluster cluster = clusters.get(0);

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                cluster.clusterType()
        );

        assertEquals(
                2,
                cluster.occurrenceCount()
        );

        assertEquals(
                List.of(ErrorPattern.HTTP_5XX),
                cluster.errorPatterns()
        );
    }

    @Test
    void groupsHttpClientErrorsIntoHttpClientFailure() {

        NormalizedLog log =
                normalizedLog(
                        "HTTP 404 Not Found",
                        ErrorPattern.HTTP_4XX
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of(log));

        assertEquals(1, clusters.size());

        assertEquals(
                LogClusterType.HTTP_CLIENT_FAILURE,
                clusters.get(0).clusterType()
        );
    }

    @Test
    void groupsConnectionRefusedIntoConnectionFailure() {

        NormalizedLog log =
                normalizedLog(
                        "Connection refused by upstream service",
                        ErrorPattern.CONNECTION_REFUSED
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of(log));

        assertEquals(1, clusters.size());

        assertEquals(
                LogClusterType.CONNECTION_FAILURE,
                clusters.get(0).clusterType()
        );
    }

    @Test
    void groupsOutOfMemoryIntoResourceExhaustion() {

        NormalizedLog log =
                normalizedLog(
                        "java.lang.OutOfMemoryError: Java heap space",
                        ErrorPattern.OUT_OF_MEMORY
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of(log));

        assertEquals(1, clusters.size());

        assertEquals(
                LogClusterType.RESOURCE_EXHAUSTION,
                clusters.get(0).clusterType()
        );
    }

    @Test
    void groupsTimeoutIntoTimeoutFailure() {

        NormalizedLog log =
                normalizedLog(
                        "Request timed out",
                        ErrorPattern.TIMEOUT
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of(log));

        assertEquals(1, clusters.size());

        assertEquals(
                LogClusterType.TIMEOUT_FAILURE,
                clusters.get(0).clusterType()
        );
    }

    @Test
    void keepsDifferentClusterTypesSeparate() {

        NormalizedLog databaseLog =
                normalizedLog(
                        "Connection pool exhausted",
                        ErrorPattern.CONNECTION_POOL_EXHAUSTED
                );

        NormalizedLog httpLog =
                normalizedLog(
                        "HTTP 500 Internal Server Error",
                        ErrorPattern.HTTP_5XX
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(
                        List.of(databaseLog, httpLog)
                );

        assertEquals(2, clusters.size());

        assertEquals(
                LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                clusters.get(0).clusterType()
        );

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                clusters.get(1).clusterType()
        );
    }

    @Test
    void ignoresUnknownErrorPatterns() {

        NormalizedLog unknown =
                normalizedLog(
                        "Something unexpected happened",
                        ErrorPattern.UNKNOWN
                );

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of(unknown));

        assertTrue(clusters.isEmpty());
    }

    @Test
    void handlesEmptyInput() {

        List<ErrorCluster> clusters =
                clusterer.cluster(List.of());

        assertTrue(clusters.isEmpty());
    }

    @Test
    void handlesNullInput() {

        List<ErrorCluster> clusters =
                clusterer.cluster(null);

        assertTrue(clusters.isEmpty());
    }

    @Test
    void ignoresNullLogs() {

        NormalizedLog timeout =
                normalizedLog(
                        "Request timed out",
                        ErrorPattern.TIMEOUT
                );

        List<NormalizedLog> logs =
                java.util.Arrays.asList(null, timeout);

        List<ErrorCluster> clusters =
                clusterer.cluster(logs);

        assertEquals(1, clusters.size());

        assertEquals(
                LogClusterType.TIMEOUT_FAILURE,
                clusters.get(0).clusterType()
        );
    }

    private NormalizedLog normalizedLog(
            String message,
            ErrorPattern errorPattern) {

        return new NormalizedLog(
                "order-service",
                LogSeverity.ERROR,
                message,
                message.toLowerCase(),
                errorPattern,
                timestamp
        );
    }
}
