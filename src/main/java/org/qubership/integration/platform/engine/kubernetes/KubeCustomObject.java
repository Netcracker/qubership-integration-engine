package org.qubership.integration.platform.engine.kubernetes;

import io.kubernetes.client.openapi.models.V1ObjectMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class KubeCustomObject {

    private final String apiVersion;
    private final String kind;
    private final V1ObjectMeta metadata;
    private final Map<String, Object> spec;
}
