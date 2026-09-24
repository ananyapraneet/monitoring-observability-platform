package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.ServiceDependency;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceDependencyCorrelatorTests {

    @Test
    void detectsDirectDependency() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "order-service"
                )
        );

        assertTrue(
                graph.dependsOn(
                        "postgres",
                        "order-service"
                )
        );
    }

    @Test
    void rejectsUnknownDependency() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "order-service"
                )
        );

        assertFalse(
                graph.dependsOn(
                        "postgres",
                        "user-service"
                )
        );
    }

    @Test
    void normalizesServiceNames() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "Postgres",
                        "Order-Service"
                )
        );

        assertTrue(
                graph.dependsOn(
                        "postgres",
                        "order-service"
                )
        );
    }

    @Test
    void returnsDownstreamServices() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "order-service"
                )
        );

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "inventory-service"
                )
        );

        assertEquals(
                List.of(
                        "order-service",
                        "inventory-service"
                ),
                graph.downstreamServices("postgres")
        );
    }

    @Test
    void correlatesAlertsAcrossDependency() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "order-service"
                )
        );

        ServiceDependencyCorrelator correlator =
                new ServiceDependencyCorrelator(graph);

        AlertEvidence postgresAlert =
                alert(
                        "PostgreSQLHighCPU",
                        "postgres"
                );

        AlertEvidence orderAlert =
                alert(
                        "OrderServiceHighLatency",
                        "order-service"
                );

        List<String> result =
                correlator.findRelatedAlerts(
                        List.of(
                                postgresAlert,
                                orderAlert
                        )
                );

        assertEquals(
                List.of(
                        "PostgreSQLHighCPU -> OrderServiceHighLatency"
                ),
                result
        );
    }

    @Test
    void returnsEmptyForUnrelatedAlerts() {

        ServiceDependencyGraph graph =
                new ServiceDependencyGraph();

        graph.addDependency(
                new ServiceDependency(
                        "postgres",
                        "order-service"
                )
        );

        ServiceDependencyCorrelator correlator =
                new ServiceDependencyCorrelator(graph);

        AlertEvidence postgresAlert =
                alert(
                        "PostgreSQLHighCPU",
                        "postgres"
                );

        AlertEvidence userAlert =
                alert(
                        "UserServiceHighLatency",
                        "user-service"
                );

        List<String> result =
                correlator.findRelatedAlerts(
                        List.of(
                                postgresAlert,
                                userAlert
                        )
                );

        assertTrue(result.isEmpty());
    }

    private AlertEvidence alert(
            String alertName,
            String service
    ) {

        return new AlertEvidence(
                alertName,
                "firing",
                "critical",
                service,
                service + ":8080",
                "Test summary",
                "Test description",
                "Test runbook",
                Map.of(),
                Instant.parse(
                        "2026-09-10T15:00:00Z"
                ),
                null
        );
    }
}
