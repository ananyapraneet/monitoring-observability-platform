package com.ananyapraneet.monitoring.incidentcontext.store;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;

import java.util.List;

public interface LogEvidenceStore {

    void add(LogEvidence log);

    List<LogEvidence> getByService(String service);

}
