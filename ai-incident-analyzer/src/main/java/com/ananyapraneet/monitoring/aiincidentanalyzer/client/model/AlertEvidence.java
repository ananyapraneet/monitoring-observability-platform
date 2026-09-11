package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import java.util.Map;

public record AlertEvidence(
        String alertName,
        String status,
        String severity,
        String service,
        String instance,
        String summary,
        String description,
        String runbook,
        Map<String, String> labels
) {}
