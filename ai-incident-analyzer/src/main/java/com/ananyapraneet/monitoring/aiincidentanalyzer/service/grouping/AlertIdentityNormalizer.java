package com.ananyapraneet.monitoring.aiincidentanalyzer.service.grouping;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class AlertIdentityNormalizer {

    public String normalizeAlertName(String alertName) {
        if (alertName == null) {
            return "";
        }

        return alertName
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public String normalizeService(String service) {
        if (service == null) {
            return "";
        }

        return service
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public String normalizeResource(AlertEvidence alert) {
        if (alert == null) {
            return "";
        }

        if (alert.instance() != null && !alert.instance().isBlank()) {
            return alert.instance()
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        String resource = alert.labels() == null
                ? null
                : alert.labels().get("resource");

        if (resource != null && !resource.isBlank()) {
            return resource
                    .trim()
                    .toLowerCase(Locale.ROOT);
        }

        return "";
    }

    public String groupKey(AlertEvidence alert) {
        String service = normalizeService(alert.service());
        String resource = normalizeResource(alert);

        return service + "|" + resource;
    }

    public String alertIdentity(AlertEvidence alert) {
        String alertName = normalizeAlertName(alert.alertName());
        String service = normalizeService(alert.service());
        String resource = normalizeResource(alert);

        return service + "|" + resource + "|" + alertName;
    }
}
