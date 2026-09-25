package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

public record RepeatedError(
        ErrorPattern errorPattern,
        int occurrenceCount) {
}
