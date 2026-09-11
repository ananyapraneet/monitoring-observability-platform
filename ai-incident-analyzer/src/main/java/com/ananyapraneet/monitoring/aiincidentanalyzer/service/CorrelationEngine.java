package com.ananyapraneet.monitoring.aiincidentanalyzer.service;

import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HealthEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.HttpErrorEvidence;
import com.ananyapraneet.monitoring.aiincidentanalyzer.client.model.IncidentContext;
import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.Correlation;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CorrelationEngine {

    public Correlation correlate(IncidentContext context) {
        if (context == null) {
            return new Correlation(
                    "No signals were available for correlation.",
                    List.of()
            );
        }

        List<String> relatedSignals = new ArrayList<>();

        boolean hasAlert = context.alerts() != null
                && !context.alerts().isEmpty();

        boolean hasMetrics = context.metrics() != null
                && !context.metrics().isEmpty();

        boolean hasLogs = context.logs() != null
                && !context.logs().isEmpty();

        boolean hasHttpErrors = context.httpErrors() != null
                && !context.httpErrors().isEmpty();

        boolean hasServerErrors = hasServerErrors(context.httpErrors());

        boolean hasClientErrors = hasClientErrors(context.httpErrors());

        boolean hasDegradedHealth = hasDegradedHealth(context.health());

        if (hasAlert) {
            relatedSignals.add("alert");
        }

        if (hasMetrics) {
            relatedSignals.add("metrics");
        }

        if (hasLogs) {
            relatedSignals.add("logs");
        }

        if (hasHttpErrors) {
            relatedSignals.add("http_errors");
        }

        if (hasDegradedHealth) {
            relatedSignals.add("degraded_health");
        }

        if (hasServerErrors && hasDegradedHealth) {
            return new Correlation(
                    "Server-side HTTP errors and degraded service health are correlated.",
                    relatedSignals
            );
        }

        if (hasServerErrors && hasMetrics) {
            return new Correlation(
                    "Server-side HTTP errors and metric signals indicate correlated API degradation.",
                    relatedSignals
            );
        }

        if (hasServerErrors) {
            return new Correlation(
                    "Server-side HTTP errors indicate API degradation.",
                    relatedSignals
            );
        }

        if (hasDegradedHealth && hasMetrics) {
            return new Correlation(
                    "Degraded service health and metric signals indicate correlated service degradation.",
                    relatedSignals
            );
        }

        if (hasDegradedHealth) {
            return new Correlation(
                    "Service health degradation is confirmed by the available health signal.",
                    relatedSignals
            );
        }

        if (hasClientErrors) {
            return new Correlation(
                    "Client-side HTTP errors indicate request or resource access failures.",
                    relatedSignals
            );
        }

        if (hasMetrics || hasLogs) {
            return new Correlation(
                    "Available metric or log signals were correlated, but they are insufficient to establish a specific failure pattern.",
                    relatedSignals
            );
        }

        return new Correlation(
                "No meaningful signals were available for correlation.",
                relatedSignals
        );
    }

    private boolean hasServerErrors(List<HttpErrorEvidence> errors) {
        if (errors == null) {
            return false;
        }

        for (HttpErrorEvidence error : errors) {
            if (error != null && error.status() >= 500 && error.status() <= 599) {
                return true;
            }
        }

        return false;
    }

    private boolean hasClientErrors(List<HttpErrorEvidence> errors) {
        if (errors == null) {
            return false;
        }

        for (HttpErrorEvidence error : errors) {
            if (error != null && error.status() >= 400 && error.status() <= 499) {
                return true;
            }
        }

        return false;
    }

    private boolean hasDegradedHealth(HealthEvidence health) {
        if (health == null || health.status() == null || health.status().isBlank()) {
            return false;
        }

        return !"UP".equalsIgnoreCase(health.status());
    }
}
