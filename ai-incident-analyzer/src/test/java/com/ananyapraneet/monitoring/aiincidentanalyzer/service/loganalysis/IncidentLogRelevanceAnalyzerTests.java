package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class IncidentLogRelevanceAnalyzerTests {

    private final IncidentLogRelevanceAnalyzer analyzer =
            new IncidentLogRelevanceAnalyzer();

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    @Test
    void mapsDatabaseConnectionAnomalyToDatabaseCluster() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        true
                                )
                        ),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType
                                .DATABASE_CONNECTION_EXHAUSTION
                ),
                result
        );
    }

    @Test
    void mapsErrorRateAnomalyToHttpServerFailure() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType.ERROR_RATE,
                                        true
                                )
                        ),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType.HTTP_SERVER_FAILURE
                ),
                result
        );
    }

    @Test
    void mapsLatencyAnomalyToTimeoutFailure() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType.LATENCY,
                                        true
                                )
                        ),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType.TIMEOUT_FAILURE
                ),
                result
        );
    }

    @Test
    void doesNotCreateClusterForRequestRateAnomalyAlone() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType.REQUEST_RATE,
                                        true
                                )
                        ),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void ignoresNonAnomalousMetricEvidence() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType
                                                .DATABASE_CONNECTIONS,
                                        false
                                )
                        ),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void mapsHttpServerErrorsToHttpServerFailure() {

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(
                                httpError(500),
                                httpError(502)
                        )
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType.HTTP_SERVER_FAILURE
                ),
                result
        );
    }

    @Test
    void mapsHttpClientErrorsToHttpClientFailure() {

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of(
                                httpError(400),
                                httpError(404)
                        )
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType.HTTP_CLIENT_FAILURE
                ),
                result
        );
    }

    @Test
    void combinesMetricAndHttpSignals() {

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
                        )
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(
                Set.of(
                        LogClusterType
                                .DATABASE_CONNECTION_EXHAUSTION,
                        LogClusterType.HTTP_SERVER_FAILURE
                ),
                result
        );
    }

    @Test
    void doesNotDuplicateClusterForMultipleSignals() {

        IncidentContext context =
                incidentContext(
                        List.of(
                                anomaly(
                                        AnomalyMetricType.ERROR_RATE,
                                        true
                                )
                        ),
                        List.of(
                                httpError(500),
                                httpError(503)
                        )
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertEquals(1, result.size());

        assertTrue(
                result.contains(
                        LogClusterType.HTTP_SERVER_FAILURE
                )
        );
    }

    @Test
    void returnsEmptyForNullContext() {

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyWhenIncidentHasNoRelevantSignals() {

        IncidentContext context =
                incidentContext(
                        List.of(),
                        List.of()
                );

        Set<LogClusterType> result =
                analyzer.determineRelevantClusters(context);

        assertTrue(result.isEmpty());
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
            List<HttpErrorEvidence> httpErrors) {

        return new IncidentContext(
                "Order service incident",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                null,
                httpErrors,
                List.of(),
                anomalies
        );
    }
}
