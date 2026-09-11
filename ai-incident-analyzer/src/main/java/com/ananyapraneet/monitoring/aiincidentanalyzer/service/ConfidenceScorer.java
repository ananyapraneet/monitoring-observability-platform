package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class ConfidenceScorer {

    public double score(
            boolean hasServerErrors,
            boolean serviceDegraded,
            Map<String, Object> metrics,
            List<?> logs) {

        if (hasServerErrors && serviceDegraded) {
            return 0.75;
        }

        if (hasServerErrors || serviceDegraded) {
            return 0.65;
        }

        if ((metrics != null && !metrics.isEmpty())
                || (logs != null && !logs.isEmpty())) {
            return 0.40;
        }

        return 0.10;
    }
}
