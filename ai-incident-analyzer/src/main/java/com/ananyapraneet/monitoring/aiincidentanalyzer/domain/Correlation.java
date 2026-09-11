package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record Correlation(
        String description,
        List<String> relatedSignals
) {}
