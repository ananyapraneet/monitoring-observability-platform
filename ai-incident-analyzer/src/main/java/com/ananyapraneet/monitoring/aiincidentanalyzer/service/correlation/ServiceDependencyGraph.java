package com.ananyapraneet.monitoring.aiincidentanalyzer.service.correlation;

import com.ananyapraneet.monitoring.aiincidentanalyzer.domain.ServiceDependency;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceDependencyGraph {

    private final Map<String, List<String>> downstreamServices =
            new HashMap<>();

    public void addDependency(
            ServiceDependency dependency
    ) {
        if (dependency == null) {
            return;
        }

        if (dependency.upstreamService() == null
                || dependency.downstreamService() == null) {
            return;
        }

        String upstream =
                normalize(dependency.upstreamService());

        String downstream =
                normalize(dependency.downstreamService());

        if (upstream.isBlank() || downstream.isBlank()) {
            return;
        }

        downstreamServices
                .computeIfAbsent(
                        upstream,
                        key -> new ArrayList<>()
                )
                .add(downstream);
    }

    public boolean dependsOn(
            String upstreamService,
            String downstreamService
    ) {
        if (upstreamService == null
                || downstreamService == null) {
            return false;
        }

        String upstream =
                normalize(upstreamService);

        String downstream =
                normalize(downstreamService);

        List<String> dependencies =
                downstreamServices.get(upstream);

        if (dependencies == null) {
            return false;
        }

        return dependencies.contains(downstream);
    }

    public List<String> downstreamServices(
            String upstreamService
    ) {
        if (upstreamService == null) {
            return List.of();
        }

        String upstream =
                normalize(upstreamService);

        List<String> dependencies =
                downstreamServices.get(upstream);

        if (dependencies == null) {
            return List.of();
        }

        return List.copyOf(dependencies);
    }

    private String normalize(String service) {
        return service.trim().toLowerCase();
    }
}
