package com.ananyapraneet.monitoring.incidentcontext.timeline;

import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.model.TimelineEvent;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class TimelineBuilder {

    public List<TimelineEvent> build(
            List<AlertmanagerAlert> alerts,
            List<HttpErrorEvidence> httpErrors,
            List<LogEvidence> logs) {

        List<TimelineEvent> timeline = new ArrayList<>();

        if (alerts != null) {
            for (AlertmanagerAlert alert : alerts) {
                addAlertEvents(timeline, alert);
            }
        }

        if (httpErrors != null) {
            for (HttpErrorEvidence error : httpErrors) {
                if (error.timestamp() == null) {
                    continue;
                }

                timeline.add(
                        new TimelineEvent(
                                error.timestamp(),
                                "HTTP_ERROR",
                                "HTTP "
                                        + error.status()
                                        + " error: "
                                        + error.method()
                                        + " "
                                        + error.endpoint()
                        )
                );
            }
        }

        if (logs != null) {
            for (LogEvidence log : logs) {
                if (log.timestamp() == null) {
                    continue;
                }

                timeline.add(
                        new TimelineEvent(
                                log.timestamp(),
                                "LOG_EVENT",
                                log.message()
                        )
                );
            }
        }

        timeline.sort(
                Comparator.comparing(
                        TimelineEvent::timestamp
                )
        );

        return List.copyOf(timeline);
    }

    private void addAlertEvents(
            List<TimelineEvent> timeline,
            AlertmanagerAlert alert) {

        if (alert == null) {
            return;
        }

        if (isValidTimestamp(alert.startsAt())) {
            String alertName = alert.labels() == null
                    ? null
                    : alert.labels().get("alertname");

            String description = alertName == null
                    ? "Alert started firing"
                    : "Alert started firing: " + alertName;

            timeline.add(
                    new TimelineEvent(
                            alert.startsAt(),
                            "ALERT_FIRING",
                            description
                    )
            );
        }

        if ("resolved".equalsIgnoreCase(alert.status())
                && isValidTimestamp(alert.endsAt())) {

            String alertName = alert.labels() == null
                    ? null
                    : alert.labels().get("alertname");

            String description = alertName == null
                    ? "Alert resolved"
                    : "Alert resolved: " + alertName;

            timeline.add(
                    new TimelineEvent(
                            alert.endsAt(),
                            "ALERT_RESOLVED",
                            description
                    )
            );
        }
    }

    private boolean isValidTimestamp(Instant timestamp) {

        if (timestamp == null) {
            return false;
        }

        return !timestamp.equals(Instant.MIN)
                && !timestamp.equals(Instant.EPOCH.minusSeconds(1))
                && timestamp.isAfter(Instant.parse("2000-01-01T00:00:00Z"));
    }
}
