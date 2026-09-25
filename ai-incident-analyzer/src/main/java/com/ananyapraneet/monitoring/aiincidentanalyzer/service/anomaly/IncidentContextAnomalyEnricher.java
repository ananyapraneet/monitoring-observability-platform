package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncidentContextAnomalyEnricher {

    private final PrometheusAnomalyDetectionService anomalyDetectionService;
    private final Duration historicalWindow;
    private final long stepSeconds;

    public IncidentContextAnomalyEnricher(
            PrometheusAnomalyDetectionService anomalyDetectionService,
            @Value("${anomaly.historical-window-minutes:30}")
            long historicalWindowMinutes,
            @Value("${anomaly.step-seconds:60}")
            long stepSeconds) {

        if (historicalWindowMinutes <= 0) {
            throw new IllegalArgumentException(
                    "Historical window must be greater than zero");
        }

        if (stepSeconds <= 0) {
            throw new IllegalArgumentException(
                    "Step seconds must be greater than zero");
        }

        this.anomalyDetectionService = anomalyDetectionService;
        this.historicalWindow =
                Duration.ofMinutes(historicalWindowMinutes);
        this.stepSeconds = stepSeconds;
    }

    public IncidentContext enrich(IncidentContext context) {

        if (context == null) {
            return null;
        }

        if (context.service() == null
                || context.service().isBlank()
                || "unknown".equalsIgnoreCase(context.service())) {

            return context;
        }

        List<AnomalyEvidence> anomalies =
                new ArrayList<>();

        String service = context.service();
        Instant end = Instant.now();

        if (isApplicationService(service)) {

            addAnomaly(
                    anomalies,
                    AnomalyMetricType.REQUEST_RATE,
                    service,
                    buildRequestRateQuery(service),
                    end
            );

            addAnomaly(
                    anomalies,
                    AnomalyMetricType.LATENCY,
                    service,
                    buildLatencyQuery(service),
                    end
            );

            addAnomaly(
                    anomalies,
                    AnomalyMetricType.ERROR_RATE,
                    service,
                    buildErrorRateQuery(service),
                    end
            );

        } else if ("postgresql".equalsIgnoreCase(service)) {

            addAnomaly(
                    anomalies,
                    AnomalyMetricType.DATABASE_CONNECTIONS,
                    service,
                    buildDatabaseConnectionQuery(),
                    end
            );
        }

        return new IncidentContext(
                context.incident(),
                context.severity(),
                context.service(),
                context.alerts(),
                context.metrics(),
                context.logs(),
                context.health(),
                context.httpErrors(),
                context.timeline(),
                anomalies
        );
    }

    private void addAnomaly(
            List<AnomalyEvidence> anomalies,
            AnomalyMetricType metricType,
            String service,
            String query,
            Instant end) {

        AnomalyResult result;

        try {
            result = anomalyDetectionService.detect(
                    metricType,
                    service,
                    query,
                    end,
                    historicalWindow,
                    stepSeconds
            );
        } catch (IllegalArgumentException exception) {
            return;
        } catch (IllegalStateException exception) {
            return;
        }

        if (result == null || !result.anomalous()) {
            return;
        }

        anomalies.add(
                new AnomalyEvidence(
                        result.metricType(),
                        result.service(),
                        result.currentValue(),
                        result.baselineMean(),
                        result.baselineStandardDeviation(),
                        result.zScore(),
                        result.anomalous(),
                        end
                )
        );
    }

    private boolean isApplicationService(String service) {

        return "gateway".equalsIgnoreCase(service)
                || "user-service".equalsIgnoreCase(service)
                || "order-service".equalsIgnoreCase(service);
    }

    private String buildRequestRateQuery(String service) {

        return "sum by (job) ("
                + "rate(http_server_requests_seconds_count{job=\""
                + service
                + "\"}[5m])"
                + ")";
    }

    private String buildLatencyQuery(String service) {

        return "histogram_quantile("
                + "0.95,"
                + "sum by (job, le) ("
                + "rate(http_server_requests_seconds_bucket{job=\""
                + service
                + "\"}[5m])"
                + ")"
                + ")";
    }

    private String buildErrorRateQuery(String service) {

        return "("
                + "sum by (job) ("
                + "rate(http_server_requests_seconds_count{"
                + "job=\""
                + service
                + "\",status=~\"5..\""
                + "}[5m])"
                + ")"
                + ") / ("
                + "sum by (job) ("
                + "rate(http_server_requests_seconds_count{"
                + "job=\""
                + service
                + "\""
                + "}[5m])"
                + ")"
                + ")";
    }

    private String buildDatabaseConnectionQuery() {

        return "("
                + "sum by (instance) ("
                + "pg_stat_database_numbackends{"
                + "datname!~\"template0|template1\""
                + "}"
                + ")"
                + ") / ("
                + "max by (instance) ("
                + "pg_settings_max_connections"
                + ")"
                + ")";
    }
}
