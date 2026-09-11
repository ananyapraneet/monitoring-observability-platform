package com.ananyapraneet.monitoring.aiincidentanalyzer.controller;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.IncidentContextClient;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.IncidentAnalysis;
import com.ananyapraneet.monitoring.aiincidentanalyzer.service.IncidentAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analyze")
public class IncidentAnalysisController {

    private final IncidentContextClient incidentContextClient;
    private final IncidentAnalyzer incidentAnalyzer;

    public IncidentAnalysisController(
            IncidentContextClient incidentContextClient,
            IncidentAnalyzer incidentAnalyzer) {
        this.incidentContextClient = incidentContextClient;
        this.incidentAnalyzer = incidentAnalyzer;
    }

    @PostMapping
    public ResponseEntity<IncidentAnalysis> analyze() {
        IncidentContext context = incidentContextClient.getLatestContext();

        IncidentAnalysis analysis = incidentAnalyzer.analyze(context);

        return ResponseEntity.ok(analysis);
    }
}
