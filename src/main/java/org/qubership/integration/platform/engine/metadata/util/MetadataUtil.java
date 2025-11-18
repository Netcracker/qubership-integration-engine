package org.qubership.integration.platform.engine.metadata.util;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.qubership.integration.platform.engine.metadata.*;

import java.util.Optional;

import static org.qubership.integration.platform.engine.model.ChainElementType.CHECKPOINT;

public class MetadataUtil {
    private MetadataUtil() {
    }

    private static String getBeanName(Class<?> cls, String id) {
        return String.format("%s-%s", cls.getSimpleName(), id);
    }

    private static <T> T getBeanForChain(Exchange exchange, Class<T> cls) {
        String chainId = getChainId(exchange);
        return exchange.getContext().getRegistry().lookupByNameAndType(getBeanName(cls, chainId), cls);
    }

    private static <T> T getBeanForElement(Exchange exchange, String elementId, Class<T> cls) {
        return exchange.getContext().getRegistry().lookupByNameAndType(getBeanName(cls, elementId), cls);
    }

    private static <T> Optional<T> getOptionalBeanForElement(Exchange exchange, String elementId, Class<T> cls) {
        return Optional.ofNullable(getBeanForElement(exchange, elementId, cls));
    }

    public static String getChainId(Exchange exchange) {
        String routeId = exchange.getFromRouteId();
        Route route = exchange.getContext().getRoute(routeId);
        return getChainId(route);
    }

    public static String getChainId(Route route) {
        return route.getGroup();
    }

    public static ChainInfo getChainInfo(Exchange exchange) {
        return getBeanForChain(exchange, ChainInfo.class);
    }

    public static boolean chainHasCheckpointElements(Exchange exchange) {
        ChainInfo chainInfo = getChainInfo(exchange);
        return exchange.getContext().getRegistry().findByType(ElementInfo.class)
                .stream()
                .anyMatch(elementInfo ->
                        chainInfo.getId().equals(elementInfo.getChainId())
                                && CHECKPOINT.getText().equals(elementInfo.getType()));
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

    public static Optional<ServiceCallInfo> getServiceCallInfo(Exchange exchange, String elementId) {
        return getOptionalBeanForElement(exchange, elementId, ServiceCallInfo.class);
    }
}
