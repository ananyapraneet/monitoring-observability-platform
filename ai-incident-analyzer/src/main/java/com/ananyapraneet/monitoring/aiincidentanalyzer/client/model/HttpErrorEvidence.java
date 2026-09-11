package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import java.time.Instant;

public record HttpErrorEvidence(
        Instant timestamp,
        String method,
        String endpoint,
        int status,
        String service,
        String requestId,
        String message
) {}
