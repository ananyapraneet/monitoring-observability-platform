package com.ananyapraneet.monitoring.incidentcontext.http;

import com.ananyapraneet.monitoring.incidentcontext.model.HttpErrorEvidence;

import java.util.List;

public interface HttpErrorClient {

    List<HttpErrorEvidence> getHttpErrors(String service);
}
