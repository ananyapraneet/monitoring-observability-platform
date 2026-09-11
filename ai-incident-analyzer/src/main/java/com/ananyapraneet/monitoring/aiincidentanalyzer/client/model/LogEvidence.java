package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import java.time.Instant;

public record LogEvidence(
        Instant timestamp,
        String level,
        String service,
        String requestId,
        String message,
        String exception
) {}
