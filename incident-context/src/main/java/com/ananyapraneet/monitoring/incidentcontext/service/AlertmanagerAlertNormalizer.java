package com.ananyapraneet.monitoring.incidentcontext.service;

import com.ananyapraneet.monitoring.incidentcontext.model.AlertEvidence;
import com.ananyapraneet.monitoring.incidentcontext.webhook.AlertmanagerAlert;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class AlertmanagerAlertNormalizer {

    public AlertEvidence normalize(AlertmanagerAlert alert) {
        Map<String, String> labels =
                alert.labels() == null ? Collections.emptyMap() : alert.labels();

        Map<String, String> annotations =
                alert.annotations() == null ? Collections.emptyMap() : alert.annotations();

        return new AlertEvidence(
                labels.get("alertname"),
                alert.status(),
                labels.get("severity"),
                labels.get("service"),
                labels.get("instance"),
                annotations.get("summary"),
                annotations.get("description"),
                annotations.get("runbook"),
                labels
        );
    }
}
