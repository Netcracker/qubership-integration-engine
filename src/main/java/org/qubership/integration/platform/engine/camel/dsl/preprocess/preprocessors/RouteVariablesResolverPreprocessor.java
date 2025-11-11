package org.qubership.integration.platform.engine.camel.dsl.preprocess.preprocessors;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.qubership.integration.platform.engine.camel.dsl.preprocess.ResourceContentPreprocessor;
import org.qubership.integration.platform.engine.metadata.RouteRegistrationInfo;
import org.qubership.integration.platform.engine.model.deployment.update.DeploymentRouteUpdate;
import org.qubership.integration.platform.engine.model.deployment.update.RouteType;
import org.qubership.integration.platform.engine.service.deployment.processing.actions.create.before.RegisterRoutesInControlPlaneAction;

import java.util.Collection;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@ApplicationScoped
@Priority(1)
public class RouteVariablesResolverPreprocessor implements ResourceContentPreprocessor {
    private final CamelContext camelContext;

    @Inject
    public RouteVariablesResolverPreprocessor(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public String apply(String content) throws Exception {
        Collection<RouteRegistrationInfo> routesRegistrationInfo = camelContext.getRegistry()
                .findByType(RouteRegistrationInfo.class)
                .stream()
                // TODO rewrite after IntegrationRuntimeService is removed
                .map(routeInfo -> DeploymentRouteUpdate.builder()
                        .path(routeInfo.getPath())
                        .type(routeInfo.getType())
                        .gatewayPrefix(routeInfo.getGatewayPrefix())
                        .variableName(routeInfo.getVariableName())
                        .connectTimeout(routeInfo.getConnectTimeout())
                        .build())
                .map(RegisterRoutesInControlPlaneAction::formatServiceRoutes)
                .map(routeUpdate -> RouteRegistrationInfo.builder()
                        .path(routeUpdate.getPath())
                        .type(routeUpdate.getType())
                        .gatewayPrefix(routeUpdate.getGatewayPrefix())
                        .variableName(routeUpdate.getVariableName())
                        .connectTimeout(routeUpdate.getConnectTimeout())
                        .build())
                .filter(this::isExternalRouteAndHasVariableName)
                .toList();

        String result = content;
        for (RouteRegistrationInfo routeInfo : routesRegistrationInfo) {
            String variablePlaceholder = String.format("%%%%{%s}", routeInfo.getVariableName());
            String gatewayPrefix = routeInfo.getGatewayPrefix();
            result = result.replace(variablePlaceholder,
                    isNull(gatewayPrefix) ? "" : gatewayPrefix);
        }
        return result;
    }

    private boolean isExternalRouteAndHasVariableName(RouteRegistrationInfo routeInfo) {
        return nonNull(routeInfo.getVariableName())
                && (RouteType.EXTERNAL_SENDER == routeInfo.getType()
                        || RouteType.EXTERNAL_SERVICE == routeInfo.getType());
    }
}
