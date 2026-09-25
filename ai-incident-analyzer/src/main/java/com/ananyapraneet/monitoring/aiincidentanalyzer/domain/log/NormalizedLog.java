package com.ananyapraneet.monitoring.aiincidentanalyzer.domain.log;

import java.time.Instant;

public record NormalizedLog(
        String service,
        LogSeverity severity,
        String originalMessage,
        String normalizedMessage,
        ErrorPattern errorPattern,
        Instant timestamp) {
}
