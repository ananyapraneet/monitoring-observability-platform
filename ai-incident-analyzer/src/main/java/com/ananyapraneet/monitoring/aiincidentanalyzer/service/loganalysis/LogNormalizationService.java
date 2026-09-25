package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.time.Instant;

public class LogNormalizationService {

    private final ErrorPatternNormalizer errorPatternNormalizer;

    public LogNormalizationService(
            ErrorPatternNormalizer errorPatternNormalizer) {

        if (errorPatternNormalizer == null) {
            throw new IllegalArgumentException(
                    "errorPatternNormalizer must not be null"
            );
        }

        this.errorPatternNormalizer = errorPatternNormalizer;
    }

    public NormalizedLog normalize(
            String service,
            LogSeverity severity,
            String message,
            Instant timestamp) {

        String normalizedMessage = normalizeMessage(message);

        ErrorPattern errorPattern =
                errorPatternNormalizer.normalize(message);

        return new NormalizedLog(
                service,
                severity,
                message,
                normalizedMessage,
                errorPattern,
                timestamp
        );
    }

    private String normalizeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        return message
                .trim()
                .toLowerCase();
    }
}
