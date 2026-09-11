package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import java.util.Map;

public record HealthEvidence(
        String status,
        Map<String, Object> components
) {}
