package org.qubership.integration.platform.engine.metadata.builders;

import org.qubership.integration.platform.engine.metadata.ChainInfo;

public class ChainInfoBuilder {
    private final ChainInfo.ChainInfoBuilder delegate;

    public ChainInfoBuilder() {
        delegate = ChainInfo.builder();
    }

    public ChainInfoBuilder id(String value) {
        delegate.id(value);
        return this;
    }

    public ChainInfoBuilder name(String value) {
        delegate.name(value);
        return this;
    }

    public ChainInfoBuilder version(String value) {
        delegate.version(value);
        return this;
    }

    public ChainInfo build() {
        return delegate.build();
    }
}
