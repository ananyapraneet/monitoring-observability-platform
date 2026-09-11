package com.ananyapraneet.monitoring.incidentcontext.controller;

import com.ananyapraneet.monitoring.incidentcontext.model.IncidentContext;
import com.ananyapraneet.monitoring.incidentcontext.service.IncidentContextBuilder;
import com.ananyapraneet.monitoring.incidentcontext.store.IncidentContextStore;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerWebhookRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertmanagerWebhookController {

    private final IncidentContextBuilder builder;
    private final IncidentContextStore store;

    public AlertmanagerWebhookController(
            IncidentContextBuilder builder,
            IncidentContextStore store) {
        this.builder = builder;
        this.store = store;
    }

    @PostMapping
    public ResponseEntity<IncidentContext> receiveAlert(
            @RequestBody AlertmanagerWebhookRequest request) {

        IncidentContext context = builder.build(request);
        store.save(context);

        return ResponseEntity.ok(context);
    }
}
