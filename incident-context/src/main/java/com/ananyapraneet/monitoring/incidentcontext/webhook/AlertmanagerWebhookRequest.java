package com.ananyapraneet.monitoring.incidentcontext.webhook;

import java.util.List;

public record AlertmanagerWebhookRequest(
        String receiver,
        String status,
        String groupKey,
        String truncatedAlerts,
        List<AlertmanagerAlert> alerts
) {
}
