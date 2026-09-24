package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import java.util.List;

public record CorrelatedIncident(

        String incident,

        String primaryService,

        String likelyRootCause,

        List<CorrelatedAlert> alerts,

        List<CorrelationType> correlationTypes,

        List<String> affectedServices,

        String explanation

) {}
