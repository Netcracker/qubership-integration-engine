package org.qubership.integration.platform.engine.camel.dsl;

import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dsl.xml.io.XmlRoutesBuilderLoader;
import org.apache.camel.spi.Resource;
import org.apache.camel.spi.annotations.RoutesLoader;

@ManagedResource(description = "Managed XML RoutesBuilderLoader")
@RoutesLoader(XmlRoutesBuilderLoader.EXTENSION)
public class CustomXmlRoutesBuilderLoader extends XmlRoutesBuilderLoader {
    @Override
    public void preParseRoute(Resource resource) throws Exception {
        super.preParseRoute(resource);
    }

    @Override
    public RouteBuilder doLoadRouteBuilder(Resource input) throws Exception {
        // TODO input text preprocessing
        return super.doLoadRouteBuilder(input);
    }
}
