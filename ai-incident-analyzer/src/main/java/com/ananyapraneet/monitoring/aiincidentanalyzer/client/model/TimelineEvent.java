package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

import java.time.Instant;

public record TimelineEvent(
        Instant timestamp,
        String type,
        String description
) {}
