package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.store.LogEvidenceStore;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StructuredLogClient implements LogClient {

    private final LogEvidenceStore logEvidenceStore;

    public StructuredLogClient(LogEvidenceStore logEvidenceStore) {
        this.logEvidenceStore = logEvidenceStore;
    }

    @Override
    public List<LogEvidence> getLogs(String service) {
        if (service == null || service.isBlank()) {
            return List.of();
        }

        return logEvidenceStore.getByService(service);
    }
}
