package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

public record Evidence(
        String type,
        String source,
        String description
) {}
