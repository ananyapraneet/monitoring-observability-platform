package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.ConfidenceScorer;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.CorrelationEngine;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.RuleBasedIncidentAnalyzer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentLogAnalysisRobustnessTests {

    @Test
    void shouldReturnEmptyMetricLogCorrelationsForNullContext() {
        IncidentMetricLogCorrelator correlator =
                createIncidentMetricLogCorrelator();

        assertEquals(
                List.of(),
                correlator.correlate(null)
        );
    }

    @Test
    void shouldReturnEmptyLogSummariesForNullContext() {
        IncidentLogSummarizer summarizer =
                createIncidentLogSummarizer();

        assertEquals(
                List.of(),
                summarizer.summarize(null)
        );
    }

    @Test
    void shouldReturnEmptyMetricLogCorrelationsWhenNoRelevantLogsExist() {
        IncidentMetricLogCorrelator correlator =
                createIncidentMetricLogCorrelator();

        IncidentContext context =
                createContextWithoutLogs();

        assertEquals(
                List.of(),
                correlator.correlate(context)
        );
    }

    @Test
    void shouldReturnEmptyLogSummariesWhenNoRelevantLogsExist() {
        IncidentLogSummarizer summarizer =
                createIncidentLogSummarizer();

        IncidentContext context =
                createContextWithoutLogs();

        assertEquals(
                List.of(),
                summarizer.summarize(context)
        );
    }

    @Test
    void shouldAllowAnalyzerWithoutOptionalLogAnalysisDependencies() {
        RuleBasedIncidentAnalyzer analyzer =
                new RuleBasedIncidentAnalyzer(
                        new CorrelationEngine(),
                        new ConfidenceScorer()
                );

        IncidentContext context =
                createContextWithoutLogs();

        IncidentAnalysis analysis =
                assertDoesNotThrow(
                        () -> analyzer.analyze(context)
                );

        assertNotNull(analysis);
    }

    @Test
    void shouldHandleIncidentContextWithEmptyAnomalyList() {
        RuleBasedIncidentAnalyzer analyzer =
                new RuleBasedIncidentAnalyzer(
                        new CorrelationEngine(),
                        new ConfidenceScorer()
                );

        IncidentContext context =
                new IncidentContext(
                        "NoAnomaliesIncident",
                        "LOW",
                        "order-service",
                        List.of(),
                        Map.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of(),
                        List.of()
                );

        IncidentAnalysis analysis =
                assertDoesNotThrow(
                        () -> analyzer.analyze(context)
                );

        assertNotNull(analysis);
        assertTrue(
                analysis.evidence().isEmpty()
        );
    }

    @Test
    void shouldPreserveAnalyzerOutputWhenLogAnalysisHasNoEvidence() {
        RuleBasedIncidentAnalyzer analyzer =
                new RuleBasedIncidentAnalyzer(
                        new CorrelationEngine(),
                        new ConfidenceScorer(),
                        null,
                        createIncidentLogSummarizer(),
                        createIncidentMetricLogCorrelator()
                );

        IncidentContext context =
                createContextWithoutLogs();

        IncidentAnalysis analysis =
                assertDoesNotThrow(
                        () -> analyzer.analyze(context)
                );

        assertNotNull(analysis);
    }

    private IncidentContext createContextWithoutLogs() {
        return new IncidentContext(
                "NoLogsIncident",
                "HIGH",
                "order-service",
                List.of(),
                Map.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of()
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
