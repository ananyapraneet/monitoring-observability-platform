package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

public record CorrelatedAlert(

        String alertName,

        String status,

        String service,

        String summary,

        String startsAt

) {}
