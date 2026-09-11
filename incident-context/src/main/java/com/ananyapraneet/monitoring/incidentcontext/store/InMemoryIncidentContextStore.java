package com.ananyapraneet.monitoring.incidentcontext.store;

import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import org.springframework.stereotype.Service;

@Service
public class InMemoryIncidentContextStore implements IncidentContextStore {

    private IncidentContext latestContext;

    @Override
    public synchronized void save(IncidentContext context) {
        this.latestContext = context;
    }

    @Override
    public synchronized IncidentContext getLatest() {
        return latestContext;
    }
}
