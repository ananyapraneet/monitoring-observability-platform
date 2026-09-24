package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.LogEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.CorrelatedIncident;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MultiSignalCorrelation;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.MetricEvidence;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentContextCorrelationAdapter {

    private static final Duration DEFAULT_TIME_WINDOW =
            Duration.ofMinutes(5);

    private final MultiSignalCorrelationEngine correlationEngine;

    private final CorrelatedIncidentMapper incidentMapper;

    public IncidentContextCorrelationAdapter(
            MultiSignalCorrelationEngine correlationEngine,
            CorrelatedIncidentMapper incidentMapper
    ) {
        this.correlationEngine =
                correlationEngine;

        this.incidentMapper =
                incidentMapper;
    }

    public CorrelatedIncident correlate(
            IncidentContext context
    ) {

        if (context == null) {
            return incidentMapper.map(null);
        }

        List<AlertEvidence> alerts =
                context.alerts() == null
                        ? List.of()
                        : context.alerts();

        List<MetricEvidence> metrics =
                mapMetrics(context);

        List<com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence>
                logs =
                mapLogs(context);

        MultiSignalCorrelation correlation =
                correlationEngine.correlate(
                        alerts,
                        metrics,
                        logs,
                        DEFAULT_TIME_WINDOW
                );

        return incidentMapper.map(correlation);
    }

    private List<MetricEvidence> mapMetrics(
            IncidentContext context
    ) {

        /*
         * IncidentContext currently exposes metrics as
         * Map<String, Object>.
         *
         * MetricEvidence requires additional structured
         * information such as threshold and timestamp.
         *
         * We therefore do not manufacture metric evidence
         * from incomplete data.
         *
         * Structured metric ingestion can be added later
         * without changing this adapter's contract.
         */

        return List.of();
    }

    private List<com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence>
    mapLogs(
            IncidentContext context
    ) {

        if (context.logs() == null
                || context.logs().isEmpty()) {

            return List.of();
        }

        List<com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence>
                logs =
                new ArrayList<>();

        for (LogEvidence log : context.logs()) {

            if (log == null) {
                continue;
            }

            logs.add(
                    new com.ananyapraneet.monitoring.aiincidentanalyzer.domain.LogEvidence(
                            log.service(),
                            "",
                            log.level(),
                            buildLogMessage(log),
                            log.timestamp()
                    )
            );
        }

        return List.copyOf(logs);
    }

    private String buildLogMessage(
            LogEvidence log
    ) {

        if (log.message() != null
                && !log.message().isBlank()) {

            return log.message();
        }

        if (log.exception() != null
                && !log.exception().isBlank()) {

            return log.exception();
        }

        return "";
    }
}
