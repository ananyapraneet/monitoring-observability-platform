package com.ananyapraneet.monitoring.incidentcontext.store;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InMemoryLogEvidenceStore implements LogEvidenceStore {

    private static final int MAX_LOGS = 1000;

    private final List<LogEvidence> logs = new ArrayList<>();

    @Override
    public synchronized void add(LogEvidence log) {
        if (log == null) {
            return;
        }

        logs.add(log);

        if (logs.size() > MAX_LOGS) {
            logs.remove(0);
        }
    }

    @Override
    public synchronized List<LogEvidence> getByService(String service) {
        if (service == null || service.isBlank()) {
            return List.of();
        }

        List<LogEvidence> result = new ArrayList<>();

        for (LogEvidence log : logs) {
            if (log == null) {
                continue;
            }

            if (service.equals(log.service())) {
                result.add(log);
            }
        }

        return List.copyOf(result);
    }
}
