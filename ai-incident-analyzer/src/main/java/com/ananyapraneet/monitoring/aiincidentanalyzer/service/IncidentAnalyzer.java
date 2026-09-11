package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;

public interface IncidentAnalyzer {

    IncidentAnalysis analyze(IncidentContext context);

}
