package com.ananyapraneet.monitoring.incidentcontext.model;

import java.util.Map;

public record HealthEvidence(
        String status,
        Map<String, Object> components
) {
}
