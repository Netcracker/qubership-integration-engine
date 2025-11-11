package org.qubership.integration.platform.engine.metadata.util;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.qubership.integration.platform.engine.metadata.ChainInfo;
import org.qubership.integration.platform.engine.metadata.ElementInfo;

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

    public static ChainInfo getChainInfo(Exchange exchange) {
        return getBeanForChain(exchange, ChainInfo.class);
    }

    public static ElementInfo getElementInfo(Exchange exchange, String elementId) {
        return exchange.getContext().getRegistry().lookupByNameAndType(elementId, ElementInfo.class);
    }
}
