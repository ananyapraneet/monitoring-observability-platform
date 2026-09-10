package com.ananyapraneet.monitoring.incidentcontext.model;

import java.time.Instant;

public record TimelineEvent(
        Instant timestamp,
        String type,
        String description
) {
}
