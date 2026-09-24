package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record MultiSignalCorrelation(
        TemporalCorrelation temporalCorrelation,
        List<String> dependencyRelationships,
        List<MetricCorrelation> metricCorrelations,
        List<LogCorrelation> logCorrelations
) {}
