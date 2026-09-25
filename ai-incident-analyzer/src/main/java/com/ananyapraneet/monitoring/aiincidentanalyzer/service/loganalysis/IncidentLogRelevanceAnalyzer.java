package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AnomalyEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.LogClusterType;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IncidentLogRelevanceAnalyzer {

    public Set<LogClusterType> determineRelevantClusters(
            IncidentContext context) {

        if (context == null) {
            return Set.of();
        }

        Set<LogClusterType> relevantClusters =
                new LinkedHashSet<>();

        addAnomalyBasedClusters(
                context.anomalies(),
                relevantClusters
        );

        addHttpBasedClusters(
                context.httpErrors(),
                relevantClusters
        );

        return Set.copyOf(relevantClusters);
    }

    private void addAnomalyBasedClusters(
            List<AnomalyEvidence> anomalies,
            Set<LogClusterType> relevantClusters) {

        if (anomalies == null || anomalies.isEmpty()) {
            return;
        }

        for (AnomalyEvidence anomaly : anomalies) {

            if (anomaly == null || !anomaly.anomalous()) {
                continue;
            }

            if (anomaly.metricType()
                    == AnomalyMetricType.DATABASE_CONNECTIONS) {

                relevantClusters.add(
                        LogClusterType
                                .DATABASE_CONNECTION_EXHAUSTION
                );
            }

            if (anomaly.metricType()
                    == AnomalyMetricType.ERROR_RATE) {

                relevantClusters.add(
                        LogClusterType.HTTP_SERVER_FAILURE
                );
            }

            if (anomaly.metricType()
                    == AnomalyMetricType.LATENCY) {

                relevantClusters.add(
                        LogClusterType.TIMEOUT_FAILURE
                );
            }
        }
    }

    private void addHttpBasedClusters(
            List<HttpErrorEvidence> httpErrors,
            Set<LogClusterType> relevantClusters) {

        if (httpErrors == null || httpErrors.isEmpty()) {
            return;
        }

        for (HttpErrorEvidence httpError : httpErrors) {

            if (httpError == null) {
                continue;
            }

            int status = httpError.status();

            if (status >= 500 && status <= 599) {
                relevantClusters.add(
                        LogClusterType.HTTP_SERVER_FAILURE
                );
            }

            if (status >= 400 && status <= 499) {
                relevantClusters.add(
                        LogClusterType.HTTP_CLIENT_FAILURE
                );
            }
        }
    }
}
