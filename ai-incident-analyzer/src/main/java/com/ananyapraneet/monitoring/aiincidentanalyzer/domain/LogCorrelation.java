package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record LogCorrelation(
        String alertName,
        List<LogEvidence> relatedLogs,
        double correlationScore
) {}
