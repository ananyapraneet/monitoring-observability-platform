package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSummary;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;

import java.util.List;

public class IncidentLogSummarizer {

    private final IncidentRelevantLogExtractor relevantLogExtractor;
    private final LogSummaryGenerator summaryGenerator;

    public IncidentLogSummarizer(
            IncidentRelevantLogExtractor relevantLogExtractor,
            LogSummaryGenerator summaryGenerator) {

        if (relevantLogExtractor == null) {
            throw new IllegalArgumentException(
                    "relevantLogExtractor must not be null"
            );
        }

        if (summaryGenerator == null) {
            throw new IllegalArgumentException(
                    "summaryGenerator must not be null"
            );
        }

        this.relevantLogExtractor =
                relevantLogExtractor;

        this.summaryGenerator =
                summaryGenerator;
    }

    public List<LogSummary> summarize(
            IncidentContext context) {

        if (context == null) {
            return List.of();
        }

        List<RelevantLog> relevantLogs =
                relevantLogExtractor.extract(context);

        if (relevantLogs.isEmpty()) {
            return List.of();
        }

        return summaryGenerator.summarize(
                relevantLogs
        );
    }
}
