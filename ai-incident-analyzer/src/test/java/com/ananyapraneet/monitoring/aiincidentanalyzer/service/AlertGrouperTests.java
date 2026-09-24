package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.AlertEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.AlertGroup;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.grouping.AlertGrouper;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.grouping.AlertIdentityNormalizer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AlertGrouperTests {

    private final AlertIdentityNormalizer normalizer =
            new AlertIdentityNormalizer();

    private final AlertGrouper grouper =
            new AlertGrouper(normalizer);

    @Test
    void groupsAlertsByServiceAndResource() {

        AlertEvidence cpu = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "postgres:5432"
        );

        AlertEvidence connections = alert(
                "PostgreSQLHighConnections",
                "postgres",
                "postgres:5432"
        );

        List<AlertGroup> groups =
                grouper.group(List.of(cpu, connections));

        assertEquals(1, groups.size());
        assertEquals("postgres|postgres:5432", groups.get(0).groupKey());
        assertEquals("postgres", groups.get(0).service());
        assertEquals("postgres:5432", groups.get(0).resource());
        assertEquals(2, groups.get(0).alerts().size());
    }

    @Test
    void separatesDifferentServices() {

        AlertEvidence postgres = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "postgres:5432"
        );

        AlertEvidence orderService = alert(
                "OrderServiceHighLatency",
                "order-service",
                "order-service:8080"
        );

        List<AlertGroup> groups =
                grouper.group(List.of(postgres, orderService));

        assertEquals(2, groups.size());
    }

    @Test
    void deduplicatesRepeatedAlerts() {

        AlertEvidence first = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "postgres:5432"
        );

        AlertEvidence duplicate = alert(
                " postgresqlhighcpu ",
                " POSTGRES ",
                " postgres:5432 "
        );

        List<AlertGroup> groups =
                grouper.group(List.of(first, duplicate));

        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).alerts().size());
    }

    @Test
    void keepsDifferentAlertTypesInSameGroup() {

        AlertEvidence cpu = alert(
                "PostgreSQLHighCPU",
                "postgres",
                "postgres:5432"
        );

        AlertEvidence connections = alert(
                "PostgreSQLConnectionFailure",
                "postgres",
                "postgres:5432"
        );

        List<AlertGroup> groups =
                grouper.group(List.of(cpu, connections));

        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).alerts().size());
    }

    @Test
    void handlesEmptyInput() {

        List<AlertGroup> groups =
                grouper.group(List.of());

        assertEquals(0, groups.size());
    }

    @Test
    void handlesNullInput() {

        List<AlertGroup> groups =
                grouper.group(null);

        assertEquals(0, groups.size());
    }

    private AlertEvidence alert(
            String alertName,
            String service,
            String instance
    ) {
        return new AlertEvidence(
                alertName,
                "firing",
                "critical",
                service,
                instance,
                "Test summary",
                "Test description",
                "Test runbook",
                Map.of(),
		null,
		null
        );
    }
}
