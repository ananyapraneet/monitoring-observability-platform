package com.ananyapraneet.monitoring.aiincidentanalyzer.domain;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;

import java.util.List;

public record AlertGroup(

        String groupKey,

        String service,

        String resource,

        List<AlertEvidence> alerts

) {}
