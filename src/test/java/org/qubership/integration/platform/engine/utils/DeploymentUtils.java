package org.qubership.integration.platform.engine.utils;

import org.qubership.integration.platform.engine.model.deployment.update.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.InputStream;

public final class DeploymentUtils {
    private DeploymentUtils() {}

    public static DeploymentUpdate getDeploymentUpdate(String filePath) throws Exception{
        return deploymentFromXml(loadXml(filePath));
    }

    public static String loadXml(String classpath) throws Exception {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath)) {
            if (is == null) throw new IllegalArgumentException("No resource: " + classpath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static DeploymentUpdate deploymentFromXml(String xml) {
        DeploymentInfo info = DeploymentInfo.builder()
                .chainId(UUID.randomUUID().toString())
                .deploymentId(UUID.randomUUID().toString())
                .chainName("test-chain")
                .snapshotId(UUID.randomUUID().toString())
                .createdWhen(System.currentTimeMillis())
                .build();

        DeploymentConfiguration cfg = DeploymentConfiguration.builder()
                .xml(xml)
                .properties(List.of())
                .routes(List.of())
                .build();

        return DeploymentUpdate.builder()
                .deploymentInfo(info)
                .configuration(cfg)
                .maskedFields(Collections.emptySet())
                .build();
    }
}
