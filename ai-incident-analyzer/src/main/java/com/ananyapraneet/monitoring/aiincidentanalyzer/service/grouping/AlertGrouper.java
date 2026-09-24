package com.ananyapraneet.monitoring.aiincidentanalyzer.service.grouping;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AlertGroup;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertGrouper {

    private final AlertIdentityNormalizer identityNormalizer;

    public AlertGrouper(AlertIdentityNormalizer identityNormalizer) {
        this.identityNormalizer = identityNormalizer;
    }

    public List<AlertGroup> group(List<AlertEvidence> alerts) {

        if (alerts == null || alerts.isEmpty()) {
            return List.of();
        }

        Map<String, AlertGroupBuilder> groups = new LinkedHashMap<>();

        for (AlertEvidence alert : alerts) {

            if (alert == null) {
                continue;
            }

            String groupKey = identityNormalizer.groupKey(alert);
            String alertIdentity = identityNormalizer.alertIdentity(alert);

            AlertGroupBuilder group = groups.computeIfAbsent(
                    groupKey,
                    key -> new AlertGroupBuilder(
                            key,
                            identityNormalizer.normalizeService(alert.service()),
                            identityNormalizer.normalizeResource(alert)
                    )
            );

            group.add(alertIdentity, alert);
        }

        return groups.values()
                .stream()
                .map(AlertGroupBuilder::build)
                .toList();
    }

    private static final class AlertGroupBuilder {

        private final String groupKey;
        private final String service;
        private final String resource;
        private final Map<String, AlertEvidence> alerts = new LinkedHashMap<>();

        private AlertGroupBuilder(
                String groupKey,
                String service,
                String resource
        ) {
            this.groupKey = groupKey;
            this.service = service;
            this.resource = resource;
        }

        private void add(
                String alertIdentity,
                AlertEvidence alert
        ) {
            alerts.putIfAbsent(alertIdentity, alert);
        }

        private AlertGroup build() {
            return new AlertGroup(
                    groupKey,
                    service,
                    resource,
                    new ArrayList<>(alerts.values())
            );
        }
    }
}
