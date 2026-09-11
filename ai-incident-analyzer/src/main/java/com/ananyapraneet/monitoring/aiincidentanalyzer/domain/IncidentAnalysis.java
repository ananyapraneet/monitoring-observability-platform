package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record IncidentAnalysis(
        String incident,
        AnalysisSeverity severity,
        String service,
        String summary,
        Correlation correlation,
        List<Evidence> evidence,
        String probableRootCause,
        List<String> recommendedRemediation,
        double confidence
) {}
