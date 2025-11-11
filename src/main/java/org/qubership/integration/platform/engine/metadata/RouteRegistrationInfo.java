package org.qubership.integration.platform.engine.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.qubership.integration.platform.engine.model.deployment.update.RouteType;

import javax.annotation.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteRegistrationInfo {
    private String path;
    @Nullable
    private String gatewayPrefix; // for senders and services
    @Nullable
    private String variableName; // to substitute with a resolved path
    private RouteType type;
    @Builder.Default
    private Long connectTimeout = -1L;
}
