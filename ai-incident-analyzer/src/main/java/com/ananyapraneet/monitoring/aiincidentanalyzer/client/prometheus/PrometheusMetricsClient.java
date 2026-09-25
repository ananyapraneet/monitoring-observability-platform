package com.ananyapraneet.monitoring.aiincidentanalyzer.client.prometheus;

import java.time.Instant;
import java.util.List;

public interface PrometheusMetricsClient {

    List<PrometheusMetricSample> queryRange(
            String query,
            Instant start,
            Instant end,
            long stepSeconds
    );

    PrometheusMetricSample queryInstant(
            String query,
            Instant timestamp
    );
}
