package com.ananyapraneet.monitoring.incidentcontext.webhook;

import java.time.Instant;
import java.util.Map;

public record AlertmanagerAlert(
        String status,
        Map<String, String> labels,
        Map<String, String> annotations,
        Instant startsAt,
        Instant endsAt,
        String generatorURL,
        String fingerprint
) {
}
