package com.ananyapraneet.monitoring.incidentcontext.store;

import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;

public interface IncidentContextStore {

    void save(IncidentContext context);

    IncidentContext getLatest();
}
