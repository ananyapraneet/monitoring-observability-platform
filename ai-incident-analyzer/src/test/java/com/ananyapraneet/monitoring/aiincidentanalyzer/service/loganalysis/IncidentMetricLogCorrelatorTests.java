package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.MetricLogCorrelation;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentMetricLogCorrelatorTests {

    private final Instant timestamp =
            Instant.parse("2026-09-25T10:15:30Z");

    private final IncidentLogRelevanceAnalyzer relevanceAnalyzer =
            new IncidentLogRelevanceAnalyzer();

    private final ErrorPatternNormalizer errorPatternNormalizer =
            new ErrorPatternNormalizer();

    private final LogEvidenceNormalizer logEvidenceNormalizer =
            new LogEvidenceNormalizer(
                    errorPatternNormalizer
            );

    private final ErrorClusterer errorClusterer =
            new ErrorClusterer();

    private final RelevantLogExtractor relevantLogExtractor =
            new RelevantLogExtractor(
                    errorClusterer
            );

    private final IncidentRelevantLogExtractor incidentRelevantLogExtractor =
            new IncidentRelevantLogExtractor(
                    relevanceAnalyzer,
                    logEvidenceNormalizer,
                    relevantLogExtractor
            );

    private final MetricLogCorrelator metricLogCorrelator =
            new MetricLogCorrelator();

    private final IncidentMetricLogCorrelator incidentCorrelator =
            new IncidentMetricLogCorrelator(
                    incidentRelevantLogExtractor,
                    metricLogCorrelator
            );

    @Test
    void correlatesDatabaseAnomalyWithRelevantDatabaseLogs() {

        IncidentContext context =
                incidentContext(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        List.of(
                                log(
                                        "Connection pool exhausted for datasource order-db"
                                ),
                                log(
                                        "Failed to acquire JDBC connection"
                                ),
                                log(
                                        "Database connection timeout"
                                )
                        )
                );

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(context);

        assertEquals(1, result.size());

        MetricLogCorrelation correlation =
                result.get(0);

        assertEquals(
                AnomalyMetricType.DATABASE_CONNECTIONS,
                correlation.metricType()
        );

        assertEquals(
                LogClusterType.DATABASE_CONNECTION_EXHAUSTION,
                correlation.clusterType()
        );

        assertEquals(
                "order-service",
                correlation.service()
        );

        assertTrue(correlation.correlated());

        assertEquals(
                3,
                correlation.logOccurrenceCount()
        );
    }

    @Test
    void correlatesErrorRateWithHttpServerLogs() {

        IncidentContext context =
                incidentContext(
                        AnomalyMetricType.ERROR_RATE,
                        List.of(
                                log(
                                        "HTTP 500 Internal Server Error"
                                ),
                                log(
                                        "HTTP 502 Bad Gateway"
                                )
                        )
                );

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(context);

        assertEquals(1, result.size());

        assertEquals(
                AnomalyMetricType.ERROR_RATE,
                result.get(0).metricType()
        );

        assertEquals(
                LogClusterType.HTTP_SERVER_FAILURE,
                result.get(0).clusterType()
        );

        assertEquals(
                2,
                result.get(0).logOccurrenceCount()
        );
    }

    @Test
    void doesNotCorrelateWhenLogsAreUnrelatedToMetricAnomaly() {

        IncidentContext context =
                incidentContext(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        List.of(
                                log(
                                        "HTTP 500 Internal Server Error"
                                )
                        )
                );

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void doesNotCorrelateWhenNoAnomaliesExist() {

        IncidentContext context =
                new IncidentContext(
                        "incident-1",
                        "CRITICAL",
                        "order-service",
                        List.of(),
                        Map.of(),
                        List.of(
                                log(
                                        "Connection pool exhausted"
                                )
                        ),
                        null,
                        List.of(),
                        List.of(),
                        List.of()
                );

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void doesNotCorrelateRequestRateWithoutKnownRelationship() {

        IncidentContext context =
                incidentContext(
                        AnomalyMetricType.REQUEST_RATE,
                        List.of(
                                log(
                                        "Connection pool exhausted"
                                )
                        )
                );

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(context);

        assertTrue(result.isEmpty());
    }

    @Test
    void returnsEmptyForNullContext() {

        List<MetricLogCorrelation> result =
                incidentCorrelator.correlate(null);

        assertTrue(result.isEmpty());
    }

    private IncidentContext incidentContext(
            AnomalyMetricType metricType,
            List<LogEvidence> logs) {

        AnomalyEvidence anomaly =
                new AnomalyEvidence(
                        metricType,
                        "order-service",
                        100.0,
                        50.0,
                        10.0,
                        5.0,
                        true,
                        timestamp
                );

        return new IncidentContext(
                "incident-1",
                "CRITICAL",
        	"order-service",
        	List.of(),
        	Map.of(),
        	logs,
        	null,
        	List.of(),
        	List.of(),
        	List.of(anomaly)
        );
    }

    private LogEvidence log(
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
}
