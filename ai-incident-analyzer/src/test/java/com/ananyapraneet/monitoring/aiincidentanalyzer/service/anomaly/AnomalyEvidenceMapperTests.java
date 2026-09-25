package com.ananyapraneet.monitoring.aiincidentanalyzer.service.anomaly;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Evidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyMetricType;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.anomaly.AnomalyResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnomalyEvidenceMapperTests {

    private final AnomalyEvidenceMapper mapper =
            new AnomalyEvidenceMapper();

    @Test
    void mapsAnomalousResultToEvidence() {

        AnomalyResult result =
                new AnomalyResult(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        450.0,
                        100.0,
                        50.0,
                        7.0,
                        true
                );

        Evidence evidence = mapper.map(result);

        assertEquals("ANOMALY", evidence.type());
        assertEquals(
                "prometheus-anomaly-detector",
                evidence.source()
        );
        assertTrue(
                evidence.description()
                        .contains("significantly different")
        );
        assertTrue(
                evidence.description()
                        .contains("450.0")
        );
        assertTrue(
                evidence.description()
                        .contains("7.0")
        );
    }

    @Test
    void mapsNormalResultToEvidence() {

        AnomalyResult result =
                new AnomalyResult(
                        AnomalyMetricType.LATENCY,
                        "order-service",
                        110.0,
                        100.0,
                        20.0,
                        0.5,
                        false
                );

        Evidence evidence = mapper.map(result);

        assertEquals("ANOMALY", evidence.type());
        assertTrue(
                evidence.description()
                        .contains("within its baseline")
        );
    }

    @Test
    void handlesNullResult() {

        Evidence evidence = mapper.map(null);

        assertNull(evidence);
    }
}
