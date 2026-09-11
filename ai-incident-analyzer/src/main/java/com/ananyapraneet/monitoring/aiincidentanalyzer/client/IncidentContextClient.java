package com.ananyapraneet.monitoring.aiincidentanalyzer.client;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;

public interface IncidentContextClient {

    IncidentContext getLatestContext();
}
