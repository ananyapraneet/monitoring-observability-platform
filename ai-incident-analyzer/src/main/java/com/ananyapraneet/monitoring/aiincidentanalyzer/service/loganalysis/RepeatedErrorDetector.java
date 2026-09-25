package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.NormalizedLog;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.RepeatedError;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RepeatedErrorDetector {

    private final int minimumOccurrences;

    public RepeatedErrorDetector(int minimumOccurrences) {

        if (minimumOccurrences < 2) {
            throw new IllegalArgumentException(
                    "minimumOccurrences must be at least 2"
            );
        }

        this.minimumOccurrences = minimumOccurrences;
    }

    public List<RepeatedError> detect(
            List<NormalizedLog> logs) {

        if (logs == null || logs.isEmpty()) {
            return List.of();
        }

        Map<ErrorPattern, Integer> occurrenceCounts =
                new LinkedHashMap<>();

        for (NormalizedLog log : logs) {
            if (log == null || log.errorPattern() == null) {
                continue;
            }

            ErrorPattern errorPattern = log.errorPattern();

            if (errorPattern == ErrorPattern.UNKNOWN) {
                continue;
            }

            occurrenceCounts.merge(
                    errorPattern,
                    1,
                    Integer::sum
            );
        }

        List<RepeatedError> repeatedErrors =
                new ArrayList<>();

        for (Map.Entry<ErrorPattern, Integer> entry
                : occurrenceCounts.entrySet()) {

            if (entry.getValue() >= minimumOccurrences) {
                repeatedErrors.add(
                        new RepeatedError(
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }
        }

        return List.copyOf(repeatedErrors);
    }
}
