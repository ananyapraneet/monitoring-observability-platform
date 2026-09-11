package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;

import java.util.List;

public interface LogClient {

    List<LogEvidence> getLogs(String service);
}
