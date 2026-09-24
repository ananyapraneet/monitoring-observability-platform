package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

public record ServiceDependency(
        String upstreamService,
        String downstreamService
) {}
