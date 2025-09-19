/*
 * Copyright 2024-2025 NetCracker Technology Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qubership.integration.platform.engine.service.deployment.processing.actions.context.create;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
//import org.apache.camel.component.jms.JmsComponent;
//import org.apache.camel.component.jms.JmsConfiguration;
//import org.apache.camel.spi.ThreadPoolProfile;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.qpid.jms.JmsConnectionFactory;
import org.qubership.integration.platform.engine.jms.weblogic.WeblogicSecureThreadFactory;
import org.qubership.integration.platform.engine.jms.weblogic.WeblogicSecurityBean;
import org.qubership.integration.platform.engine.jms.weblogic.WeblogicSecurityInterceptStrategy;
import org.qubership.integration.platform.engine.model.ChainElementType;
import org.qubership.integration.platform.engine.model.constants.CamelConstants.ChainProperties;
import org.qubership.integration.platform.engine.model.deployment.update.DeploymentInfo;
import org.qubership.integration.platform.engine.model.deployment.update.ElementProperties;
import org.qubership.integration.platform.engine.service.VariablesService;
import org.qubership.integration.platform.engine.service.deployment.processing.ElementProcessingAction;
import org.qubership.integration.platform.engine.service.deployment.processing.qualifiers.OnAfterDeploymentContextCreated;
//import org.springframework.jms.support.destination.JndiDestinationResolver;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
//import java.util.Properties;
//import javax.naming.Context;

@ApplicationScoped
@OnAfterDeploymentContextCreated
public class JmsElementDependencyBinder extends ElementProcessingAction {
    private final VariablesService variablesService;
    private final Instance<WeblogicSecurityBean> wlSecurityBeanProvider;
    private final Instance<WeblogicSecurityInterceptStrategy> wlSecurityInterceptStrategyProvider;
    private final Instance<WeblogicSecureThreadFactory> wlSecureThreadFactoryProvider;

    @Inject
    public JmsElementDependencyBinder(
        VariablesService variablesService,
        Instance<WeblogicSecurityBean> wlSecurityBeanProvider,
        Instance<WeblogicSecurityInterceptStrategy> wlSecurityInterceptStrategyProvider,
        Instance<WeblogicSecureThreadFactory> wlSecureThreadFactoryProvider
    ) {
        this.variablesService = variablesService;
        this.wlSecurityBeanProvider = wlSecurityBeanProvider;
        this.wlSecurityInterceptStrategyProvider = wlSecurityInterceptStrategyProvider;
        this.wlSecureThreadFactoryProvider = wlSecureThreadFactoryProvider;
    }

    @Override
    public boolean applicableTo(ElementProperties properties) {
        ChainElementType elementType = ChainElementType.fromString(
                properties.getProperties().get(ChainProperties.ELEMENT_TYPE));
        return ChainElementType.JMS_SENDER.equals(elementType)
                || ChainElementType.JMS_TRIGGER.equals(elementType);
    }

    @Override
    public void apply(
        CamelContext context,
        ElementProperties elementProperties,
        DeploymentInfo deploymentInfo
    ) {
        // FIXME [migration to quarkus]
        //
        //        String elementId = elementProperties.getElementId();
        //        Map<String, String> properties = elementProperties.getProperties();
        //        Properties environment = new Properties();
        //        String jmsInitialContextFactory = variablesService.injectVariables(
        //            properties.get(ChainProperties.JMS_INITIAL_CONTEXT_FACTORY));
        //        String jmsProviderUrl = variablesService.injectVariables(properties.get(
        //            ChainProperties.JMS_PROVIDER_URL));
        //        String jmsConnectionFactoryName = variablesService.injectVariables(
        //            properties.get(ChainProperties.JMS_CONNECTION_FACTORY_NAME));
        //
        //        String username = variablesService.injectVariables(properties.get(
        //            ChainProperties.JMS_USERNAME));
        //        String password = variablesService.injectVariables(properties.get(
        //            ChainProperties.JMS_PASSWORD));
        //
        //        environment.put(Context.INITIAL_CONTEXT_FACTORY, jmsInitialContextFactory);
        //        environment.put(Context.PROVIDER_URL, jmsProviderUrl);
        //
        //        boolean secured = !StringUtils.isBlank(username) && !StringUtils.isBlank(password);
        //        if (secured) {
        //            environment.put(Context.SECURITY_PRINCIPAL, username);
        //            environment.put(Context.SECURITY_CREDENTIALS, password);
        //        }
        //
        //        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(username, password, jmsProviderUrl);
        //
        //        JndiDestinationResolver jndiDestinationResolver = new JndiDestinationResolver();
        //        jndiDestinationResolver.setFallbackToDynamicDestination(true);
        //
        //        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        //        jmsConfiguration.setConnectionFactory(jmsConnectionFactory);
        //        jmsConfiguration.setDestinationResolver(jndiDestinationResolver);
        //
        //        WeblogicSecurityBean wlSecurityBean = wlSecurityBeanProvider.getIfAvailable();
        //        WeblogicSecureThreadFactory wlSecureThreadFactory = wlSecureThreadFactoryProvider.getIfAvailable();
        //        WeblogicSecurityInterceptStrategy wlSecurityInterceptStrategy = wlSecurityInterceptStrategyProvider.getIfAvailable();
        //        if (secured && wlSecurityBean != null && wlSecureThreadFactory != null
        //                && wlSecurityInterceptStrategy != null
        //        ) {
        //            wlSecurityBean.setProviderUrl(jmsProviderUrl);
        //            wlSecurityBean.setSecurityPrincipal(username);
        //            wlSecurityBean.setSecurityCredentials(password);
        //
        //            wlSecureThreadFactory.setName("jms-thread-factory-" + elementId);
        //            wlSecureThreadFactory.setWeblogicSecurityBean(wlSecurityBean);
        //
        //            ThreadPoolProfile profile = context.getExecutorServiceManager().getDefaultThreadPoolProfile();
        //            ThreadPoolTaskExecutor jmsTaskExecutor = new ThreadPoolTaskExecutor();
        //            jmsTaskExecutor.setBeanName("jms-task-executor-" + elementId);
        //            jmsTaskExecutor.setThreadFactory(wlSecureThreadFactory);
        //            jmsTaskExecutor.setCorePoolSize(profile.getPoolSize());
        //            jmsTaskExecutor.setMaxPoolSize(profile.getMaxPoolSize());
        //            jmsTaskExecutor.setKeepAliveSeconds(profile.getKeepAliveTime().intValue());
        //            jmsTaskExecutor.setQueueCapacity(profile.getMaxQueueSize());
        //            jmsTaskExecutor.afterPropertiesSet();
        //
        //            jmsConfiguration.setTaskExecutor(jmsTaskExecutor);
        //
        //            wlSecurityInterceptStrategy.setTargetId(elementId);
        //            wlSecurityInterceptStrategy.setWeblogicSecurityBean(wlSecurityBean);
        //
        //            context.getCamelContextExtension().addInterceptStrategy(wlSecurityInterceptStrategy);
        //        }
        //
        //        JmsComponent jmsComponent = new JmsComponent(jmsConfiguration);
        //
        //        String componentName = buildJmsComponentName(elementId, properties);
        //        context.addComponent(componentName, jmsComponent);
    }

    private String buildJmsComponentName(String elementId, Map<String, String> properties) {
        return String.format("jms-%s", elementId);
    }
}
