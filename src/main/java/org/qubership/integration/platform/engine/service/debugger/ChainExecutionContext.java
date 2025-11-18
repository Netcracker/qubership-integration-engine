package org.qubership.integration.platform.engine.service.debugger;

import lombok.Builder;
import lombok.Getter;
import org.qubership.integration.platform.engine.metadata.ChainInfo;
import org.qubership.integration.platform.engine.metadata.ElementInfo;
import org.qubership.integration.platform.engine.model.ChainElementType;
import org.qubership.integration.platform.engine.model.deployment.properties.ChainRuntimeProperties;

@Getter
@Builder(toBuilder = true)
public class ChainExecutionContext {
    private ChainInfo chainInfo;
    private ChainRuntimeProperties chainRuntimeProperties;
    private String stepId;
    private String stepName;
    private ElementInfo elementInfo;

    public ChainElementType getElementType() {
        return ChainElementType.fromString(elementInfo.getType());
    }
}
