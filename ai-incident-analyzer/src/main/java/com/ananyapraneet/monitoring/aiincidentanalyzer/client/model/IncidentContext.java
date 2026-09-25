package com.ananyapraneet.monitoring.aiincidentanalyzer.client.model;

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

        List<TimelineEvent> timeline,

        List<AnomalyEvidence> anomalies

) {

    public IncidentContext(

            String incident,

            String severity,

            String service,

            List<AlertEvidence> alerts,

            Map<String, Object> metrics,

            List<LogEvidence> logs,

            HealthEvidence health,

            List<HttpErrorEvidence> httpErrors,

            List<TimelineEvent> timeline) {

        this(
                incident,
                severity,
                service,
                alerts,
                metrics,
                logs,
                health,
                httpErrors,
                timeline,
                List.of()
        );
    }
}
