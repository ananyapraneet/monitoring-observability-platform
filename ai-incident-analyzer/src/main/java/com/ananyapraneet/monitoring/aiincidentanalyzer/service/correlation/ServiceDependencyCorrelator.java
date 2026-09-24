package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceDependencyCorrelator {

    private final ServiceDependencyGraph dependencyGraph;

    public ServiceDependencyCorrelator(
            ServiceDependencyGraph dependencyGraph
    ) {
        this.dependencyGraph = dependencyGraph;
    }

    public List<String> findRelatedAlerts(
            List<AlertEvidence> alerts
    ) {

        if (alerts == null || alerts.size() < 2) {
            return List.of();
        }

        List<String> relatedAlerts =
                new ArrayList<>();

        for (int i = 0; i < alerts.size(); i++) {

            AlertEvidence source =
                    alerts.get(i);

            if (source == null
                    || source.service() == null) {
                continue;
            }

            for (int j = i + 1; j < alerts.size(); j++) {

                AlertEvidence target =
                        alerts.get(j);

                if (target == null
                        || target.service() == null) {
                    continue;
                }

                if (dependencyGraph.dependsOn(
                        source.service(),
                        target.service()
                )) {

                    relatedAlerts.add(
                            source.alertName()
                                    + " -> "
                                    + target.alertName()
                    );

                } else if (dependencyGraph.dependsOn(
                        target.service(),
                        source.service()
                )) {

                    relatedAlerts.add(
                            target.alertName()
                                    + " -> "
                                    + source.alertName()
                    );
                }
            }
        }

        return relatedAlerts;
    }
}
