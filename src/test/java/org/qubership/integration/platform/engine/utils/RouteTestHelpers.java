package org.qubership.integration.platform.engine.utils;

import org.apache.camel.CamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;


import static org.apache.camel.xml.jaxb.JaxbHelper.loadRoutesDefinition;

public final class RouteTestHelpers {
    private RouteTestHelpers() {}

    /** Возвращает uri первого <from .../> из XML. */
    public static String entryFromUri(CamelContext ctx, String xml) throws Exception {
        RoutesDefinition def = loadRoutesDefinition(ctx, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        List<RouteDefinition> routes = def.getRoutes();
        if (routes.isEmpty() || routes.get(0).getInput() == null) {
            throw new IllegalStateException("No <from> found in the first route.");
        }
        return routes.get(0).getInput().getUri();
    }
}
