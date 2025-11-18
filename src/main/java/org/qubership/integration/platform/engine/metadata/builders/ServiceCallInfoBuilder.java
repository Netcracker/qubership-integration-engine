package org.qubership.integration.platform.engine.metadata.builders;

import org.qubership.integration.platform.engine.metadata.ServiceCallInfo;

public class ServiceCallInfoBuilder {
    private final ServiceCallInfo.ServiceCallInfoBuilder delegate;

    public ServiceCallInfoBuilder() {
        delegate = ServiceCallInfo.builder();
    }

    public ServiceCallInfoBuilder retryCount(String retryCount) {
        delegate.retryCount(retryCount);
        return this;
    }

    public ServiceCallInfoBuilder retryDelay(String retryDelay) {
        delegate.retryDelay(retryDelay);
        return this;
    }

    public ServiceCallInfoBuilder externalServiceName(String externalServiceName) {
        delegate.externalServiceName(externalServiceName);
        return this;
    }

    public ServiceCallInfoBuilder externalServiceEnvironmentName(String externalServiceEnvironmentName) {
        delegate.externalServiceEnvironmentName(externalServiceEnvironmentName);
        return this;
    }

    public ServiceCallInfoBuilder protocol(String protocol) {
        delegate.protocol(protocol);
        return this;
    }

    public ServiceCallInfo build() {
        return delegate.build();
    }
}
