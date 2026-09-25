package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

public enum ErrorPattern {

    CONNECTION_POOL_EXHAUSTED,
    DATABASE_CONNECTION_TIMEOUT,
    DATABASE_CONNECTION_FAILURE,
    OUT_OF_MEMORY,
    HTTP_5XX,
    HTTP_4XX,
    TIMEOUT,
    CONNECTION_REFUSED,
    UNKNOWN
}
