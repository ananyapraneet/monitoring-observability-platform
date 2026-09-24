package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LogCorrelator {

    public LogCorrelation correlate(
            AlertEvidence alert,
            List<LogEvidence> logs
    ) {

        if (alert == null) {
            return new LogCorrelation(
                    null,
                    List.of(),
                    0.0
            );
        }

        if (logs == null || logs.isEmpty()) {
            return new LogCorrelation(
                    alert.alertName(),
                    List.of(),
                    0.0
            );
        }

        List<LogEvidence> relatedLogs =
                new ArrayList<>();

        for (LogEvidence log : logs) {

            if (log == null) {
                continue;
            }

            if (!matchesService(alert, log)) {
                continue;
            }

            if (!matchesResource(alert, log)) {
                continue;
            }

            if (!isRelevantLogLevel(log)) {
                continue;
            }

            relatedLogs.add(log);
        }

        double correlationScore =
                calculateScore(relatedLogs);

        return new LogCorrelation(
                alert.alertName(),
                List.copyOf(relatedLogs),
                correlationScore
        );
    }

    private boolean matchesService(
            AlertEvidence alert,
            LogEvidence log
    ) {

        if (alert.service() == null
                || log.service() == null) {
            return false;
        }

        return alert.service()
                .trim()
                .equalsIgnoreCase(
                        log.service().trim()
                );
    }

    private boolean matchesResource(
            AlertEvidence alert,
            LogEvidence log
    ) {

        if (log.resource() == null
                || log.resource().isBlank()) {
            return true;
        }

        if (alert.instance() == null
                || alert.instance().isBlank()) {
            return false;
        }

        return alert.instance()
                .trim()
                .equalsIgnoreCase(
                        log.resource().trim()
                );
    }

    private boolean isRelevantLogLevel(
            LogEvidence log
    ) {

        if (log.level() == null) {
            return false;
        }

        String level =
                log.level()
                        .trim()
                        .toUpperCase();

        return level.equals("ERROR")
                || level.equals("WARN")
                || level.equals("WARNING");
    }

    private double calculateScore(
            List<LogEvidence> relatedLogs
    ) {

        if (relatedLogs.isEmpty()) {
            return 0.0;
        }

        if (relatedLogs.size() == 1) {
            return 0.5;
        }

        if (relatedLogs.size() == 2) {
            return 0.75;
        }

        return 1.0;
    }
}
