package org.qubership.integration.platform.engine.metadata.util;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.qubership.integration.platform.engine.metadata.ChainInfo;
import org.qubership.integration.platform.engine.metadata.ElementInfo;
import org.qubership.integration.platform.engine.metadata.MaskedFields;
import org.qubership.integration.platform.engine.metadata.WireTapInfo;

import java.util.Optional;

public class MetadataUtil {
    private MetadataUtil() {
    }

    public static String getChainId(Exchange exchange) {
        String routeId = exchange.getFromRouteId();
        Route route = exchange.getContext().getRoute(routeId);
        return route.getGroup();
    }

    public static <T> T getBeanForChain(Exchange exchange, Class<T> cls) {
        String chainId = getChainId(exchange);
        return exchange.getContext().getRegistry().lookupByNameAndType(chainId, cls);
    }

    public static <T> T getBeanForElement(Exchange exchange, String elementId, Class<T> cls) {
        return exchange.getContext().getRegistry().lookupByNameAndType(elementId, cls);
    }

    public static <T> Optional<T> getOptionalBeanForElement(Exchange exchange, String elementId, Class<T> cls) {
        return Optional.ofNullable(getBeanForElement(exchange, elementId, cls));
    }

    public static ChainInfo getChainInfo(Exchange exchange) {
        return getBeanForChain(exchange, ChainInfo.class);
    }

    public static MaskedFields getMaskedFields(Exchange exchange) {
        return getBeanForChain(exchange, MaskedFields.class);
    }

    public static Optional<ElementInfo> getElementInfo(Exchange exchange, String elementId) {
        return getOptionalBeanForElement(exchange, elementId, ElementInfo.class);
    }

    public static Optional<WireTapInfo> getWireTapInfo(Exchange exchange, String elementId) {
        return getOptionalBeanForElement(exchange, elementId, WireTapInfo.class);
    }
}
