package com.ananyapraneet.monitoring.incidentcontext.health;

import com.ananyapraneet.monitoring.incidentcontext.model.HealthEvidence;

public interface HealthClient {

    HealthEvidence getHealth(String service);
}
