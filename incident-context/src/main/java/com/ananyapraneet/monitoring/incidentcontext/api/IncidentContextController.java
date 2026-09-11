package com.ananyapraneet.monitoring.incidentcontext.api;

import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import com.ananyapraneet.monitoring.incidentcontext.store.IncidentContextStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/context")
public class IncidentContextController {

    private final IncidentContextStore store;

    public IncidentContextController(IncidentContextStore store) {
        this.store = store;
    }

    @GetMapping("/latest")
    public ResponseEntity<IncidentContext> getLatestContext() {

        IncidentContext context = store.getLatest();

        if (context == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(context);
    }
}
