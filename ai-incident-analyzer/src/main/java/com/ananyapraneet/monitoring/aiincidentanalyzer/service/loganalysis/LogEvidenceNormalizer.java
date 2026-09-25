package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogSeverity;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;

import java.util.ArrayList;
import java.util.List;

public class LogEvidenceNormalizer {

    private final ErrorPatternNormalizer errorPatternNormalizer;

    public LogEvidenceNormalizer(
            ErrorPatternNormalizer errorPatternNormalizer) {

        if (errorPatternNormalizer == null) {
            throw new IllegalArgumentException(
                    "errorPatternNormalizer must not be null"
            );
        }

        this.errorPatternNormalizer =
                errorPatternNormalizer;
    }

    public List<NormalizedLog> normalize(
            List<LogEvidence> logs) {

        if (logs == null || logs.isEmpty()) {
            return List.of();
        }

        List<NormalizedLog> normalizedLogs =
                new ArrayList<>();

        for (LogEvidence log : logs) {

            if (log == null) {
                continue;
            }

            String message = buildMessage(log);

            ErrorPattern errorPattern =
                    errorPatternNormalizer.normalize(message);

            LogSeverity severity =
                    parseSeverity(log.level());

            String normalizedMessage =
                    message == null
                            ? ""
                            : message.trim().toLowerCase();

            normalizedLogs.add(
                    new NormalizedLog(
                            log.service(),
                            severity,
                            message,
                            normalizedMessage,
                            errorPattern,
                            log.timestamp()
                    )
            );
        }

        return List.copyOf(normalizedLogs);
    }

    private String buildMessage(
            LogEvidence log) {

        String message = log.message();

        if (log.exception() == null
                || log.exception().isBlank()) {
            return message;
        }

        if (message == null || message.isBlank()) {
            return log.exception();
        }

        return message + " " + log.exception();
    }

    private LogSeverity parseSeverity(
            String level) {

        if (level == null || level.isBlank()) {
            return LogSeverity.INFO;
        }

        try {
            return LogSeverity.valueOf(
                    level.trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            return LogSeverity.INFO;
        }
    }
}
