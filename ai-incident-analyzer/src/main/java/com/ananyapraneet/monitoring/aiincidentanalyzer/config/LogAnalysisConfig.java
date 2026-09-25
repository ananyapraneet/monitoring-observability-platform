package com.ananyapraneet.monitoring.aiincidentanalyzer.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogAnalysisConfig {

    @Bean
    public ErrorPatternNormalizer errorPatternNormalizer() {
        return new ErrorPatternNormalizer();
    }

    @Bean
    public ErrorClusterer errorClusterer() {
        return new ErrorClusterer();
    }

    @Bean
    public RelevantLogExtractor relevantLogExtractor(
            ErrorClusterer errorClusterer) {

        return new RelevantLogExtractor(
                errorClusterer
        );
    }

    @Bean
    public IncidentLogRelevanceAnalyzer
            incidentLogRelevanceAnalyzer() {

        return new IncidentLogRelevanceAnalyzer();
    }

    @Bean
    public LogEvidenceNormalizer logEvidenceNormalizer(
            ErrorPatternNormalizer errorPatternNormalizer) {

        return new LogEvidenceNormalizer(
                errorPatternNormalizer
        );
    }

    @Bean
    public IncidentRelevantLogExtractor
            incidentRelevantLogExtractor(
                    IncidentLogRelevanceAnalyzer
                            incidentLogRelevanceAnalyzer,
                    LogEvidenceNormalizer
                            logEvidenceNormalizer,
                    RelevantLogExtractor
                            relevantLogExtractor) {

        return new IncidentRelevantLogExtractor(
                incidentLogRelevanceAnalyzer,
                logEvidenceNormalizer,
                relevantLogExtractor
        );
    }

    @Bean
    public LogSummaryGenerator logSummaryGenerator() {
        return new LogSummaryGenerator();
    }

    @Bean
    public IncidentLogSummarizer incidentLogSummarizer(
            IncidentRelevantLogExtractor
                    incidentRelevantLogExtractor,
            LogSummaryGenerator logSummaryGenerator) {

        return new IncidentLogSummarizer(
                incidentRelevantLogExtractor,
                logSummaryGenerator
        );
    }

    @Bean
    public MetricLogCorrelator metricLogCorrelator() {
        return new MetricLogCorrelator();
    }

    @Bean
    public IncidentMetricLogCorrelator
            incidentMetricLogCorrelator(
                    IncidentRelevantLogExtractor
                            incidentRelevantLogExtractor,
                    MetricLogCorrelator metricLogCorrelator) {

        return new IncidentMetricLogCorrelator(
                incidentRelevantLogExtractor,
                metricLogCorrelator
        );
    }
}
