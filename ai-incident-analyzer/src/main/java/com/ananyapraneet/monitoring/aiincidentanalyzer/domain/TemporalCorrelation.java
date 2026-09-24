package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.time.Duration;
import java.util.List;

public record TemporalCorrelation(

        List<CorrelatedAlert> alerts,

        Duration timeWindow,

        String parentAlert,

        List<String> symptomAlerts,

        double correlationScore

) {}
