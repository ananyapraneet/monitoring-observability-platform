package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

public enum LogClusterType {

    DATABASE_CONNECTION_EXHAUSTION,
    DATABASE_CONNECTIVITY_FAILURE,
    HTTP_SERVER_FAILURE,
    HTTP_CLIENT_FAILURE,
    RESOURCE_EXHAUSTION,
    TIMEOUT_FAILURE,
    CONNECTION_FAILURE,
    UNKNOWN
}
