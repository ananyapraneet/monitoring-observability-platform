package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfidenceScorerTests {

    private final ConfidenceScorer scorer = new ConfidenceScorer();

    @Test
    void shouldGiveHighestConfidenceWhenServerErrorsAndHealthDegradationCorrelate() {
        double confidence = scorer.score(
                true,
                true,
                Map.of(),
                List.of()
        );

        assertEquals(0.75, confidence);
    }

    @Test
    void shouldGiveModerateConfidenceForServerErrorsAlone() {
        double confidence = scorer.score(
                true,
                false,
                Map.of(),
                List.of()
        );

        assertEquals(0.65, confidence);
    }

    @Test
    void shouldGiveModerateConfidenceForDegradedHealthAlone() {
        double confidence = scorer.score(
                false,
                true,
                Map.of(),
                List.of()
        );

        assertEquals(0.65, confidence);
    }

    @Test
    void shouldGiveLowConfidenceForMetricsOrLogsOnly() {
        double metricsConfidence = scorer.score(
                false,
                false,
                Map.of("request_rate", 10.0),
                List.of()
        );

        double logsConfidence = scorer.score(
                false,
                false,
                Map.of(),
                List.of("log-entry")
        );

        assertEquals(0.40, metricsConfidence);
        assertEquals(0.40, logsConfidence);
    }

    @Test
    void shouldGiveMinimalConfidenceWhenEvidenceIsMissing() {
        double confidence = scorer.score(
                false,
                false,
                Map.of(),
                List.of()
        );

        assertEquals(0.10, confidence);
    }

    @Test
    void shouldHandleNullEvidenceCollectionsSafely() {
        double confidence = scorer.score(
                false,
                false,
                null,
                null
        );

        assertEquals(0.10, confidence);
    }
}
