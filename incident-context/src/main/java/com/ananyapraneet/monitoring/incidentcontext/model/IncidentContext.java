package com.ananyapraneet.monitoring.incidentcontext.model;

import java.util.List;
import java.util.Map;

public record IncidentContext(
        String incident,
        String severity,
        String service,
        List<AlertEvidence> alerts,
        Map<String, Object> metrics,
        List<LogEvidence> logs,
        HealthEvidence health,
        List<HttpErrorEvidence> httpErrors,
        List<TimelineEvent> timeline
) {
}
