package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MultiSignalCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.TemporalCorrelation;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class MultiSignalCorrelationEngine {

    private final TemporalCorrelator temporalCorrelator;
    private final ServiceDependencyCorrelator serviceDependencyCorrelator;
    private final MetricCorrelator metricCorrelator;
    private final LogCorrelator logCorrelator;

    public MultiSignalCorrelationEngine(
            TemporalCorrelator temporalCorrelator,
            ServiceDependencyCorrelator serviceDependencyCorrelator,
            MetricCorrelator metricCorrelator,
            LogCorrelator logCorrelator
    ) {
        this.temporalCorrelator =
                temporalCorrelator;

        this.serviceDependencyCorrelator =
                serviceDependencyCorrelator;

        this.metricCorrelator =
                metricCorrelator;

        this.logCorrelator =
                logCorrelator;
    }

    public MultiSignalCorrelation correlate(
            List<AlertEvidence> alerts,
            List<MetricEvidence> metrics,
            List<LogEvidence> logs,
            Duration timeWindow
    ) {

        if (timeWindow == null
            	|| timeWindow.isZero()
            	|| timeWindow.isNegative()) {

            throw new IllegalArgumentException(
                    "Time window must be positive"
            );
    	}

        if (alerts == null || alerts.isEmpty()) {

            return new MultiSignalCorrelation(
                    temporalCorrelator.correlate(
                            List.of(),
                            timeWindow
                    ),
                    List.of(),
                    List.of(),
                    List.of()
            );
        }

        TemporalCorrelation temporalCorrelation =
                temporalCorrelator.correlate(
                        alerts,
                        timeWindow
                );

        List<String> dependencyRelationships =
                serviceDependencyCorrelator.findRelatedAlerts(
                        alerts
                );

        List<MetricCorrelation> metricCorrelations =
                new ArrayList<>();

        List<LogCorrelation> logCorrelations =
                new ArrayList<>();

        for (AlertEvidence alert : alerts) {

            if (alert == null) {
                continue;
            }

            MetricCorrelation metricCorrelation =
                    metricCorrelator.correlate(
                            alert,
                            metrics
                    );

            if (!metricCorrelation.relatedMetrics().isEmpty()) {
                metricCorrelations.add(
                        metricCorrelation
                );
            }

            LogCorrelation logCorrelation =
                    logCorrelator.correlate(
                            alert,
                            logs
                    );

            if (!logCorrelation.relatedLogs().isEmpty()) {
                logCorrelations.add(
                        logCorrelation
                );
            }
        }

        return new MultiSignalCorrelation(
                temporalCorrelation,
                List.copyOf(dependencyRelationships),
                List.copyOf(metricCorrelations),
                List.copyOf(logCorrelations)
        );
    }
}
