package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSummary;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class LogSummaryGeneratorTests {

    private final LogSummaryGenerator generator =
            new LogSummaryGenerator();

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void summarizesRepeatedDatabaseErrors() {

        List<RelevantLog> logs =
                List.of(
                        relevantLog(
                                "Connection pool exhausted",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        relevantLog(
                                "Failed to acquire JDBC connection",
                                ErrorPattern.DATABASE_CONNECTION_FAILURE,
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        relevantLog(
                                "Database connection timeout",
                                ErrorPattern.DATABASE_CONNECTION_TIMEOUT,
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        )
                );

        List<LogSummary> result =
                generator.summarize(logs);

        assertEquals(1, result.size());

        LogSummary summary =
                result.get(0);

        assertEquals(
                "order-service",
                summary.service()
        );

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
    void summarizesSingleHttpServerError() {

        List<RelevantLog> logs =
                List.of(
                        relevantLog(
                                "HTTP 500 Internal Server Error",
                                ErrorPattern.HTTP_5XX,
                                LogClusterType
                                        .HTTP_SERVER_FAILURE
                        )
                );

        List<LogSummary> result =
                generator.summarize(logs);

        assertEquals(1, result.size());

        LogSummary summary =
                result.get(0);

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                summary.clusterType()
        );

        assertEquals(
                1,
                summary.occurrenceCount()
        );

        assertEquals(
                "HTTP 5xx server errors was observed in Order Service.",
                summary.summary()
        );
    }

    @Test
    void createsSeparateSummariesForDifferentClusters() {

        List<RelevantLog> logs =
                List.of(
                        relevantLog(
                                "Connection pool exhausted",
                                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                                LogClusterType
                                        .DATABASE_CONNECTION_EXHAUSTION
                        ),
                        relevantLog(
                                "HTTP 500 Internal Server Error",
                                ErrorPattern.HTTP_5XX,
                                LogClusterType
                                        .HTTP_SERVER_FAILURE
                        )
                );

        List<LogSummary> result =
                generator.summarize(logs);

        assertEquals(2, result.size());

        assertEquals(
                LogClusterType
                        .DATABASE_CONNECTION_EXHAUSTION,
                result.get(0).clusterType()
        );

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                result.get(1).clusterType()
        );
    }

    @Test
    void formatsServiceName() {

        List<RelevantLog> logs =
                List.of(
                        relevantLog(
                                "HTTP 500",
                                ErrorPattern.HTTP_5XX,
                                LogClusterType
                                        .HTTP_SERVER_FAILURE
                        )
                );

        List<LogSummary> result =
                generator.summarize(logs);

        assertTrue(
                result.get(0).summary()
                        .contains("Order Service")
        );
    }

    @Test
    void ignoresNullRelevantLogs() {

        List<RelevantLog> logs =
                new java.util.ArrayList<>();

    	logs.add(null);

    	logs.add(
            	relevantLog(
                    	"Connection pool exhausted",
                    	ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                    	LogClusterType
                            	.DATABASE_CONNECTION_EXHAUSTION
            	)
    	);

        List<LogSummary> result =
                generator.summarize(logs);

        assertEquals(1, result.size());
    }

    @Test
    void returnsEmptyForEmptyInput() {

        List<LogSummary> result =
                generator.summarize(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullInput() {

        List<LogSummary> result =
                generator.summarize(null);

        assertTrue(result.isEmpty());
    }

    private RelevantLog relevantLog(
            String message,
            ErrorPattern errorPattern,
            LogClusterType clusterType) {

        NormalizedLog log =
                new NormalizedLog(
                        "order-service",
                        LogSeverity.ERROR,
                        message,
                        message.toLowerCase(),
                        errorPattern,
                        timestamp
                );

        return new RelevantLog(
                log,
                clusterType,
                "Matches a relevant cluster"
        );
    }
}
