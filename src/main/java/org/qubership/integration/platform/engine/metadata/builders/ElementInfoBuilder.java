package org.qubership.integration.platform.engine.metadata.builders;

import org.qubership.integration.platform.engine.metadata.ElementInfo;

public class ElementInfoBuilder {
    private final ElementInfo.ElementInfoBuilder delegate;

    public ElementInfoBuilder() {
        this.delegate = ElementInfo.builder();
    }

    public ElementInfoBuilder id(String value) {
        delegate.id(value);
        return this;
    }

    public ElementInfoBuilder name(String value) {
        delegate.name(value);
        return this;
    }

    public ElementInfoBuilder type(String value) {
        delegate.type(value);
        return this;
    }

    public ElementInfoBuilder chainId(String value) {
        delegate.chainId(value);
        return this;
    }

    public ElementInfoBuilder parentId(String value) {
        delegate.parentId(value);
        return this;
    }

    public ElementInfoBuilder reuseId(String value) {
        delegate.reuseId(value);
        return this;
    }

    public ElementInfoBuilder hasIntermediateParents(boolean value) {
        delegate.hasIntermediateParents(value);
        return this;
    }

    public ElementInfo build() {
        return delegate.build();
    }
}
