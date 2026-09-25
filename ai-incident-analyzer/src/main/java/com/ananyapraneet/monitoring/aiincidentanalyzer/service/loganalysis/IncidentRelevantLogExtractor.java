package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RelevantLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.util.List;
import java.util.Set;

public class IncidentRelevantLogExtractor {

    private final IncidentLogRelevanceAnalyzer relevanceAnalyzer;
    private final LogEvidenceNormalizer logEvidenceNormalizer;
    private final RelevantLogExtractor logExtractor;

    public IncidentRelevantLogExtractor(
            IncidentLogRelevanceAnalyzer relevanceAnalyzer,
            LogEvidenceNormalizer logEvidenceNormalizer,
            RelevantLogExtractor logExtractor) {

        if (relevanceAnalyzer == null) {
            throw new IllegalArgumentException(
                    "relevanceAnalyzer must not be null"
            );
        }

        if (logEvidenceNormalizer == null) {
            throw new IllegalArgumentException(
                    "logEvidenceNormalizer must not be null"
            );
        }

        if (logExtractor == null) {
            throw new IllegalArgumentException(
                    "logExtractor must not be null"
            );
        }

        this.relevanceAnalyzer = relevanceAnalyzer;
        this.logEvidenceNormalizer = logEvidenceNormalizer;
        this.logExtractor = logExtractor;
    }

    public List<RelevantLog> extract(
            IncidentContext context) {

        if (context == null) {
            return List.of();
        }

        Set<LogClusterType> relevantClusters =
                relevanceAnalyzer.determineRelevantClusters(
                        context
                );

        if (relevantClusters.isEmpty()) {
            return List.of();
        }

        List<NormalizedLog> normalizedLogs =
                logEvidenceNormalizer.normalize(
                        context.logs()
                );

        if (normalizedLogs.isEmpty()) {
            return List.of();
        }

        return logExtractor.extract(
                normalizedLogs,
                relevantClusters,
                context.service()
        );
    }
}
