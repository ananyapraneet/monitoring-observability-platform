package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedAlert;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedIncident;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelationType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MultiSignalCorrelation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class CorrelatedIncidentMapper {

    public CorrelatedIncident map(
            MultiSignalCorrelation correlation
    ) {

        if (correlation == null) {
            return new CorrelatedIncident(
                    "No correlated incident identified.",
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of(),
                    "No correlation evidence was available."
            );
        }

        List<CorrelatedAlert> alerts =
                mapAlerts(correlation);

        List<CorrelationType> correlationTypes =
                determineCorrelationTypes(correlation);

        List<String> affectedServices =
                determineAffectedServices(alerts);

        String primaryService =
                determinePrimaryService(alerts);

        String incident =
                buildIncidentDescription(
                        correlationTypes
                );

        String explanation =
                buildExplanation(
                        correlation,
                        correlationTypes
                );

        return new CorrelatedIncident(
                incident,
                primaryService,
                null,
                alerts,
                correlationTypes,
                affectedServices,
                explanation
        );
    }

    private List<CorrelatedAlert> mapAlerts(
            MultiSignalCorrelation correlation
    ) {

        if (correlation.temporalCorrelation() == null
                || correlation.temporalCorrelation().alerts() == null) {

            return List.of();
        }

        List<CorrelatedAlert> alerts =
                new ArrayList<>();

        for (var alert :
                correlation.temporalCorrelation().alerts()) {

            if (alert == null) {
                continue;
            }

            alerts.add(
                    new CorrelatedAlert(
                            alert.alertName(),
                            alert.status(),
                            alert.service(),
                            alert.summary(),
                            alert.startsAt()
                    )
            );
        }

        return List.copyOf(alerts);
    }

    private List<CorrelationType> determineCorrelationTypes(
            MultiSignalCorrelation correlation
    ) {

        List<CorrelationType> types =
                new ArrayList<>();

        if (hasTemporalCorrelation(correlation)) {
            types.add(CorrelationType.TEMPORAL);
        }

        if (correlation.dependencyRelationships() != null
                && !correlation.dependencyRelationships().isEmpty()) {

            types.add(
                    CorrelationType.SERVICE_DEPENDENCY
            );
        }

        if (correlation.metricCorrelations() != null
                && !correlation.metricCorrelations().isEmpty()) {

            types.add(
                    CorrelationType.METRIC
            );
        }

        if (correlation.logCorrelations() != null
                && !correlation.logCorrelations().isEmpty()) {

            types.add(
                    CorrelationType.LOG
            );
        }

        if (types.size() > 1) {
            types.add(CorrelationType.MULTI_SIGNAL);
        }

        return List.copyOf(types);
    }

    private boolean hasTemporalCorrelation(
            MultiSignalCorrelation correlation
    ) {

        if (correlation.temporalCorrelation() == null) {
            return false;
        }

        return correlation.temporalCorrelation()
                .alerts() != null
                && !correlation.temporalCorrelation()
                .alerts()
                .isEmpty();
    }

    private List<String> determineAffectedServices(
            List<CorrelatedAlert> alerts
    ) {

        Set<String> services =
                new LinkedHashSet<>();

        for (CorrelatedAlert alert : alerts) {

            if (alert == null
                    || alert.service() == null
                    || alert.service().isBlank()) {

                continue;
            }

            services.add(
                    alert.service().trim()
            );
        }

        return List.copyOf(services);
    }

    private String determinePrimaryService(
            List<CorrelatedAlert> alerts
    ) {

        if (alerts.isEmpty()) {
            return null;
        }

        CorrelatedAlert firstAlert =
                alerts.get(0);

        return firstAlert.service();
    }

    private String buildIncidentDescription(
            List<CorrelationType> correlationTypes
    ) {

        if (correlationTypes.isEmpty()) {
            return "No meaningful correlation identified.";
        }

        if (correlationTypes.contains(
                CorrelationType.MULTI_SIGNAL)) {

            return "Multiple signals indicate a correlated incident.";
        }

        return "Correlation evidence indicates a related incident.";
    }

    private String buildExplanation(
            MultiSignalCorrelation correlation,
            List<CorrelationType> correlationTypes
    ) {

        if (correlationTypes.isEmpty()) {
            return "No meaningful correlation evidence was identified.";
        }

        List<String> evidence =
                new ArrayList<>();

        if (correlationTypes.contains(
                CorrelationType.TEMPORAL)) {

            evidence.add(
                    "alerts occurred within the configured time window"
            );
        }

        if (correlationTypes.contains(
                CorrelationType.SERVICE_DEPENDENCY)) {

            evidence.add(
                    correlation.dependencyRelationships().size()
                            + " service dependency relationship(s) were identified"
            );
        }

        if (correlationTypes.contains(
                CorrelationType.METRIC)) {

            evidence.add(
                    correlation.metricCorrelations().size()
                            + " metric correlation(s) were identified"
            );
        }

        if (correlationTypes.contains(
                CorrelationType.LOG)) {

            evidence.add(
                    correlation.logCorrelations().size()
                            + " log correlation(s) were identified"
            );
        }

        return "Correlation evidence: "
                + String.join("; ", evidence)
                + ".";
    }
}
