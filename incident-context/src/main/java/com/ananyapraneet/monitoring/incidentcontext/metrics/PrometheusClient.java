package com.ananyapraneet.monitoring.incidentcontext.metrics;

import java.util.Map;

public interface PrometheusClient {

    Map<String, Object> queryMetrics(String service);
}
