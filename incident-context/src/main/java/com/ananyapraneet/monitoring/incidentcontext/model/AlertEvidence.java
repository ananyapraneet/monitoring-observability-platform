package com.ananyapraneet.monitoring.incidentcontext.model;

import java.time.Instant;
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
        Map<String, String> labels,
        Instant startsAt,
        Instant endsAt

) {

}
