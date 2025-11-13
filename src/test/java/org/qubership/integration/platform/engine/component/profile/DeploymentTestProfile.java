package org.qubership.integration.platform.engine.component.profile;

import io.quarkus.test.junit.QuarkusTestProfile;
import org.qubership.integration.platform.engine.utils.PostgresTwoDbTestResource;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeploymentTestProfile implements QuarkusTestProfile {

    private static final String EXCLUDED_TYPES = Stream.of(
            "org.qubership.integration.platform.engine.consul.**",
            "org.qubership.integration.platform.engine.rest.**",
            "org.qubership.integration.platform.engine.scheduler.*",
            "org.qubership.integration.platform.engine.healthcheck.*",

            "org.qubership.integration.platform.engine.service.suspend.**",
            "org.qubership.integration.platform.engine.service.testing.**",
            "org.qubership.integration.platform.engine.service.xmlpreprocessor.**",

            "org.qubership.integration.platform.engine.service.deployment.actions.**",
            "org.qubership.integration.platform.engine.service.deployment.qualifiers.**",
            "org.qubership.integration.platform.engine.service.deployment.",

            "org.qubership.integration.platform.engine.service.DeploymentsUpdateService",
            "org.qubership.integration.platform.engine.service.IntegrationRuntimeService",
            "org.qubership.integration.platform.engine.service.debugger.metrics.SessionsMetricsService",
            "org.qubership.integration.platform.engine.service.debugger.metrics.SessionsMetricsServiceProducer",
            "org.qubership.integration.platform.engine.configuration.opensearch.**",
            "org.qubership.integration.platform.engine.service.debugger.sessions.**",
            "org.qubership.integration.platform.engine.service.debugger.CamelDebugger",
            "org.qubership.integration.platform.engine.configuration.SessionsMetricsServiceProducer"

    ).collect(Collectors.joining(","));

    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.ofEntries(
                Map.entry("quarkus.arc.exclude-types", EXCLUDED_TYPES),

                Map.entry("quarkus.consul-config.enabled", "false"),
                Map.entry("consul.enabled", "false"),
                Map.entry("consul.keys.prefix", "qip/tests"),
                Map.entry("consul.token", "dummy"),

                Map.entry("qip.metrics.enabled", "false")
        );
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(PostgresTwoDbTestResource.class));
    }
}
