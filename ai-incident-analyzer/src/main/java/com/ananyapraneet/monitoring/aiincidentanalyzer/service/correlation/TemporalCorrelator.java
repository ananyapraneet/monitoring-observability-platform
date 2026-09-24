package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedAlert;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.TemporalCorrelation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TemporalCorrelator {

    public TemporalCorrelation correlate(
            List<AlertEvidence> alerts,
            Duration timeWindow
    ) {

        if (timeWindow == null || timeWindow.isZero() || timeWindow.isNegative()) {

    	    throw new IllegalArgumentException(
            	    "Time window must be positive"
    	    );
        }

        if (alerts == null || alerts.isEmpty()) {

            return new TemporalCorrelation(
                    List.of(),
                    timeWindow,
                    null,
                    List.of(),
                    0.0
            );
        }

        List<AlertEvidence> timestampedAlerts = new ArrayList<>();

        for (AlertEvidence alert : alerts) {

            if (alert == null) {
                continue;
            }

            if (alert.startsAt() == null) {
                continue;
            }

            timestampedAlerts.add(alert);
        }

        if (timestampedAlerts.isEmpty()) {
            return new TemporalCorrelation(
                    List.of(),
                    timeWindow,
                    null,
                    List.of(),
                    0.0
            );
        }

        timestampedAlerts.sort(
                Comparator.comparing(AlertEvidence::startsAt)
        );

        Instant firstTimestamp =
                timestampedAlerts.get(0).startsAt();

        List<AlertEvidence> correlatedAlerts =
                new ArrayList<>();

        for (AlertEvidence alert : timestampedAlerts) {

            Duration elapsed =
                    Duration.between(
                            firstTimestamp,
                            alert.startsAt()
                    );

            if (elapsed.compareTo(timeWindow) <= 0) {
                correlatedAlerts.add(alert);
            }
        }

        List<CorrelatedAlert> correlatedAlertModels =
                new ArrayList<>();

        for (AlertEvidence alert : correlatedAlerts) {

            correlatedAlertModels.add(
                    new CorrelatedAlert(
                            alert.alertName(),
                            alert.status(),
                            alert.service(),
                            alert.summary(),
                            alert.startsAt().toString()
                    )
            );
        }

        String parentAlert =
                correlatedAlerts.get(0).alertName();

        List<String> symptomAlerts =
                new ArrayList<>();

        for (int i = 1; i < correlatedAlerts.size(); i++) {
            symptomAlerts.add(
                    correlatedAlerts.get(i).alertName()
            );
        }

        double correlationScore =
                calculateScore(
                        correlatedAlerts,
                        timeWindow
                );

        return new TemporalCorrelation(
                correlatedAlertModels,
                timeWindow,
                parentAlert,
                symptomAlerts,
                correlationScore
        );
    }

    private double calculateScore(
            List<AlertEvidence> alerts,
            Duration timeWindow
    ) {

        if (alerts.size() < 2) {
            return 0.0;
        }

        Duration elapsed =
                Duration.between(
                        alerts.get(0).startsAt(),
                        alerts.get(alerts.size() - 1).startsAt()
                );

        if (elapsed.isZero()) {
            return 1.0;
        }

        double elapsedSeconds =
                elapsed.toMillis() / 1000.0;

        double windowSeconds =
                timeWindow.toMillis() / 1000.0;

        double score =
                1.0 - (elapsedSeconds / windowSeconds);

        if (score < 0.0) {
            return 0.0;
        }

        if (score > 1.0) {
            return 1.0;
        }

        return score;
    }
}
