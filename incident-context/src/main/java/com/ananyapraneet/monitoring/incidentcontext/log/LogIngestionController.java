package com.ananyapraneet.monitoring.incidentcontext.log;

import com.ananyapraneet.monitoring.incidentcontext.model.LogEvidence;
import com.ananyapraneet.monitoring.incidentcontext.store.LogEvidenceStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
public class LogIngestionController {

    private final LogEvidenceStore logEvidenceStore;

    public LogIngestionController(LogEvidenceStore logEvidenceStore) {
        this.logEvidenceStore = logEvidenceStore;
    }

    @PostMapping
    public ResponseEntity<Void> ingest(
            @RequestBody List<LogEvidence> logs) {

        if (logs == null || logs.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        for (LogEvidence log : logs) {
            if (log == null
                    || log.timestamp() == null
                    || log.level() == null
                    || log.service() == null
                    || log.message() == null) {
                return ResponseEntity.badRequest().build();
            }
        }

        for (LogEvidence log : logs) {
            logEvidenceStore.add(log);
        }

        return ResponseEntity.accepted().build();
    }
}
