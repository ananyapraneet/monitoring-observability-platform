package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructuredLogClient implements LogClient {

    @Override
    public List<LogEvidence> getLogs(String service) {

        if (service == null || service.isBlank()) {
            return List.of();
        }

        /*
         * Log ingestion will be connected to the platform's centralized
         * logging backend in the next observability stage.
         *
         * For now, return an empty collection rather than reading local
         * container logs directly.
         */
        return List.of();
    }
}
