package com.ananyapraneet.monitoring.aiincidentanalyzer.service.loganalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log.ErrorPattern;
import org.junit.jupiter.api.Test;

class ErrorPatternNormalizerTests {

    private final ErrorPatternNormalizer normalizer =
            new ErrorPatternNormalizer();

    @Test
    void detectsConnectionPoolExhaustion() {
        ErrorPattern result = normalizer.normalize(
                "Connection pool exhausted for datasource order-db"
        );

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result
        );
    }

    @Test
    void detectsDatabaseConnectionTimeout() {
        ErrorPattern result = normalizer.normalize(
                "Database connection timeout while processing order"
        );

        assertEquals(
                ErrorPattern.DATABASE_CONNECTION_TIMEOUT,
                result
        );
    }

    @Test
    void detectsDatabaseConnectionFailure() {
        ErrorPattern result = normalizer.normalize(
                "Failed to acquire JDBC connection"
        );

        assertEquals(
                ErrorPattern.DATABASE_CONNECTION_FAILURE,
                result
        );
    }

    @Test
    void detectsOutOfMemory() {
        ErrorPattern result = normalizer.normalize(
                "java.lang.OutOfMemoryError: Java heap space"
        );

        assertEquals(
                ErrorPattern.OUT_OF_MEMORY,
                result
        );
    }

    @Test
    void detectsHttpServerError() {
        ErrorPattern result = normalizer.normalize(
                "HTTP 500 Internal Server Error"
        );

        assertEquals(
                ErrorPattern.HTTP_5XX,
                result
        );
    }

    @Test
    void detectsHttpClientError() {
        ErrorPattern result = normalizer.normalize(
                "HTTP 404 Not Found"
        );

        assertEquals(
                ErrorPattern.HTTP_4XX,
                result
        );
    }

    @Test
    void detectsTimeout() {
        ErrorPattern result = normalizer.normalize(
                "Request timed out while calling payment service"
        );

        assertEquals(
                ErrorPattern.TIMEOUT,
                result
        );
    }

    @Test
    void detectsConnectionRefused() {
        ErrorPattern result = normalizer.normalize(
                "Connection refused by upstream service"
        );

        assertEquals(
                ErrorPattern.CONNECTION_REFUSED,
                result
        );
    }

    @Test
    void returnsUnknownForUnrecognizedMessage() {
        ErrorPattern result = normalizer.normalize(
                "Something unexpected happened"
        );

        assertEquals(
                ErrorPattern.UNKNOWN,
                result
        );
    }

    @Test
    void returnsUnknownForNullMessage() {
        ErrorPattern result = normalizer.normalize(null);

        assertEquals(
                ErrorPattern.UNKNOWN,
                result
        );
    }

    @Test
    void matchingIsCaseInsensitive() {
        ErrorPattern result = normalizer.normalize(
                "CONNECTION POOL EXHAUSTED"
        );

        assertEquals(
                ErrorPattern.CONNECTION_POOL_EXHAUSTED,
                result
        );
    }
}
