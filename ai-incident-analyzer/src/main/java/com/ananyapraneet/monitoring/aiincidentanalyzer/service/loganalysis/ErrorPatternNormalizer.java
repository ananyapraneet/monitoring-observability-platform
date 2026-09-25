package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;

public class ErrorPatternNormalizer {

    public ErrorPattern normalize(String message) {
        if (message == null || message.isBlank()) {
            return ErrorPattern.UNKNOWN;
        }

        String normalizedMessage = message.toLowerCase();

        if (containsAny(
                normalizedMessage,
                "connection pool exhausted",
                "pool exhausted",
                "hikaripool"
        )) {
            return ErrorPattern.CONNECTION_POOL_EXHAUSTED;
        }

        if (containsAny(
                normalizedMessage,
                "database connection timeout",
                "db connection timeout",
                "connection timed out"
        )) {
            return ErrorPattern.DATABASE_CONNECTION_TIMEOUT;
        }

        if (containsAny(
                normalizedMessage,
                "failed to acquire jdbc connection",
                "cannot obtain connection",
                "database connection failed",
                "jdbc connection failed"
        )) {
            return ErrorPattern.DATABASE_CONNECTION_FAILURE;
        }

        if (containsAny(
                normalizedMessage,
                "outofmemoryerror",
                "out of memory",
                "java heap space",
                "heap space"
        )) {
            return ErrorPattern.OUT_OF_MEMORY;
        }

        if (containsAny(
        	normalizedMessage,
        	"http 500",
        	"http 501",
        	"http 502",
        	"http 503",
        	"http 504",
        	"http 505",
        	"http status 500",
        	"http status 501",
        	"http status 502",
        	"http status 503",
        	"http status 504",
        	"http status 505",
        	"internal server error",
        	"bad gateway",
        	"service unavailable",
        	"gateway timeout",
        	"status code 5"
	)) {
    	    return ErrorPattern.HTTP_5XX;
	}

        if (containsAny(
                normalizedMessage,
                "http 400",
                "http 401",
                "http 403",
                "http 404",
                "http status 4",
                "status code 4"
        )) {
            return ErrorPattern.HTTP_4XX;
        }

        if (containsAny(
                normalizedMessage,
                "request timed out",
                "request timeout",
                "operation timed out",
                "timeout"
        )) {
            return ErrorPattern.TIMEOUT;
        }

        if (containsAny(
                normalizedMessage,
                "connection refused",
                "connect refused"
        )) {
            return ErrorPattern.CONNECTION_REFUSED;
        }

        return ErrorPattern.UNKNOWN;
    }

    private boolean containsAny(
            String message,
            String... patterns) {

        for (String pattern : patterns) {
            if (message.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
