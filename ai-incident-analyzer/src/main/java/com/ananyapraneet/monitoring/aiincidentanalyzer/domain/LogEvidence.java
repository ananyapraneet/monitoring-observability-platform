package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.time.Instant;

public record LogEvidence(
        String service,
        String resource,
        String level,
        String message,
        Instant timestamp
) {}
