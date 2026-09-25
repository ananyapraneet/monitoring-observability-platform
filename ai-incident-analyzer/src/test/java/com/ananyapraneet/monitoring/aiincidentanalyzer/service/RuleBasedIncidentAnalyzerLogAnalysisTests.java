package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation.IncidentContextCorrelationAdapter;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.ErrorClusterer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.ErrorPatternNormalizer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentLogRelevanceAnalyzer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentLogSummarizer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentMetricLogCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.IncidentRelevantLogExtractor;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.LogEvidenceNormalizer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.LogSummaryGenerator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.MetricLogCorrelator;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis.RelevantLogExtractor;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleBasedIncidentAnalyzerLogAnalysisTests {

    private final RuleBasedIncidentAnalyzer analyzer =
            new RuleBasedIncidentAnalyzer(
                    new CorrelationEngine(),
                    new ConfidenceScorer(),
                    (IncidentContextCorrelationAdapter) null,
                    createIncidentLogSummarizer(),
                    createIncidentMetricLogCorrelator()
            );

    @Test
    void shouldAddLogSummaryEvidenceWhenRelevantLogsExist() {

        IncidentContext context =
                createContextWithDatabaseConnectionLogs();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "LOG_SUMMARY".equals(evidence.type())
                        )
        );
    }

    @Test
    void shouldAddMetricLogCorrelationEvidenceWhenMetricAndLogsAlign() {

        IncidentContext context =
                createContextWithDatabaseConnectionLogs();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "METRIC_LOG_CORRELATION"
                                        .equals(evidence.type())
                        )
        );
    }

    @Test
    void shouldPreserveExistingEvidenceAlongsideLogAnalysisEvidence() {

        IncidentContext context =
                createContextWithDatabaseConnectionLogs();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "HTTP_ERROR".equals(evidence.type())
                        )
        );

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "LOG_SUMMARY".equals(evidence.type())
                        )
        );

        assertTrue(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "METRIC_LOG_CORRELATION"
                                        .equals(evidence.type())
                        )
        );
    }

    @Test
    void shouldNotAddLogAnalysisEvidenceWhenNoLogsExist() {

        IncidentContext context =
                new IncidentContext(
                        "NoLogsIncident",
                        "HIGH",
                        "order-service",
                        List.of(),
                        Map.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of()
                );

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertFalse(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "LOG_SUMMARY".equals(evidence.type())
                        )
        );

        assertFalse(
                analysis.evidence()
                        .stream()
                        .anyMatch(evidence ->
                                "METRIC_LOG_CORRELATION"
                                        .equals(evidence.type())
                        )
        );
    }

    @Test
    void shouldNotTreatMetricLogCorrelationAsProofOfCausality() {

        IncidentContext context =
                createContextWithDatabaseConnectionLogs();

        IncidentAnalysis analysis =
                analyzer.analyze(context);

        assertNotNull(analysis);

        assertFalse(
                analysis.probableRootCause()
                        .toLowerCase()
                        .contains("caused by")
        );

        assertFalse(
                analysis.probableRootCause()
                        .toLowerCase()
                        .contains("proven")
        );

        assertTrue(
                analysis.evidence()
                        .stream()
                        .filter(evidence ->
                                "METRIC_LOG_CORRELATION"
                                        .equals(evidence.type())
                        )
                        .allMatch(evidence ->
                                evidence.description()
                                        .toLowerCase()
                                        .contains("not proof")
                        )
        );
    }

    private IncidentContext createContextWithDatabaseConnectionLogs() {

        Instant timestamp =
                Instant.parse("2026-09-25T10:00:00Z");

        List<LogEvidence> logs = List.of(

                new LogEvidence(
                        timestamp,
                        "ERROR",
                        "order-service",
                        "request-1",
                        "Failed to acquire database connection",
                        "SQLTransientConnectionException"
                ),

                new LogEvidence(
                        timestamp.plusSeconds(30),
                        "ERROR",
                        "order-service",
                        "request-2",
                        "Connection pool exhausted",
                        "SQLTransientConnectionException"
                ),

                new LogEvidence(
                        timestamp.plusSeconds(60),
                        "ERROR",
                        "order-service",
                        "request-3",
                        "Unable to obtain connection from pool",
                        "SQLTransientConnectionException"
                )
        );

        AnomalyEvidence databaseConnectionAnomaly =
                new AnomalyEvidence(
                        AnomalyMetricType.DATABASE_CONNECTIONS,
                        "order-service",
                        96.0,
                        60.0,
                        5.0,
                        7.2,
                        true,
                        timestamp
                );

        List<HttpErrorEvidence> httpErrors =
                List.of(
                        new HttpErrorEvidence(
                                timestamp.plusSeconds(90),
                                "GET",
                                "/orders",
                                500,
                                "order-service",
                                "request-4",
                                "Internal server error"
                        )
                );

        return new IncidentContext(
                "DatabaseConnectionIncident",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(
                        "database_connections",
                        96.0
                ),
                logs,
                null,
                httpErrors,
                List.of(),
                List.of(databaseConnectionAnomaly)
        );
    }

    private IncidentLogSummarizer createIncidentLogSummarizer() {

        ErrorPatternNormalizer errorPatternNormalizer =
                new ErrorPatternNormalizer();

        ErrorClusterer errorClusterer =
                new ErrorClusterer();

        RelevantLogExtractor relevantLogExtractor =
                new RelevantLogExtractor(
                        errorClusterer
                );

        IncidentLogRelevanceAnalyzer
                incidentLogRelevanceAnalyzer =
                new IncidentLogRelevanceAnalyzer();

        LogEvidenceNormalizer logEvidenceNormalizer =
                new LogEvidenceNormalizer(
                        errorPatternNormalizer
                );

        IncidentRelevantLogExtractor
                incidentRelevantLogExtractor =
                new IncidentRelevantLogExtractor(
                        incidentLogRelevanceAnalyzer,
                        logEvidenceNormalizer,
                        relevantLogExtractor
                );

        LogSummaryGenerator logSummaryGenerator =
                new LogSummaryGenerator();

        return new IncidentLogSummarizer(
                incidentRelevantLogExtractor,
                logSummaryGenerator
        );
    }

    private IncidentMetricLogCorrelator
            createIncidentMetricLogCorrelator() {

        ErrorPatternNormalizer errorPatternNormalizer =
                new ErrorPatternNormalizer();

        ErrorClusterer errorClusterer =
                new ErrorClusterer();

        RelevantLogExtractor relevantLogExtractor =
                new RelevantLogExtractor(
                        errorClusterer
                );

        IncidentLogRelevanceAnalyzer
                incidentLogRelevanceAnalyzer =
                new IncidentLogRelevanceAnalyzer();

        LogEvidenceNormalizer logEvidenceNormalizer =
                new LogEvidenceNormalizer(
                        errorPatternNormalizer
                );

        IncidentRelevantLogExtractor
                incidentRelevantLogExtractor =
                new IncidentRelevantLogExtractor(
                        incidentLogRelevanceAnalyzer,
                        logEvidenceNormalizer,
                        relevantLogExtractor
                );

        MetricLogCorrelator metricLogCorrelator =
                new MetricLogCorrelator();

        return new IncidentMetricLogCorrelator(
                incidentRelevantLogExtractor,
                metricLogCorrelator
        );
    }
}
