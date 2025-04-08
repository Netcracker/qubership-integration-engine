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

package org.qubership.integration.platform.engine.service;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.Getter;
import org.apache.camel.FailedToCreateRouteException;
import org.apache.camel.FailedToStartRouteException;
import org.apache.camel.component.jackson.JacksonConstants;
import org.apache.camel.impl.engine.DefaultManagementStrategy;
import org.apache.camel.impl.engine.DefaultStreamCachingStrategy;
import org.apache.camel.model.*;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.observation.MicrometerObservationTracer;
import org.apache.camel.reifier.ProcessorReifier;
import org.apache.camel.spi.MessageHistoryFactory;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.camel.tracing.Tracer;
import org.codehaus.groovy.control.CompilationFailedException;
import org.jetbrains.annotations.NotNull;
import org.qubership.integration.platform.engine.camel.CustomResilienceReifier;
import org.qubership.integration.platform.engine.camel.QipCustomClassResolver;
import org.qubership.integration.platform.engine.camel.context.propagation.constant.BusinessIds;
import org.qubership.integration.platform.engine.camel.converters.FormDataConverter;
import org.qubership.integration.platform.engine.camel.converters.SecurityAccessPolicyConverter;
import org.qubership.integration.platform.engine.camel.history.FilteringMessageHistoryFactory;
import org.qubership.integration.platform.engine.camel.history.FilteringMessageHistoryFactory.FilteringEntity;
import org.qubership.integration.platform.engine.configuration.ServerConfiguration;
import org.qubership.integration.platform.engine.configuration.TracingConfiguration;
import org.qubership.integration.platform.engine.consul.DeploymentReadinessService;
import org.qubership.integration.platform.engine.consul.EngineStateReporter;
import org.qubership.integration.platform.engine.errorhandling.DeploymentRetriableException;
import org.qubership.integration.platform.engine.errorhandling.KubeApiException;
import org.qubership.integration.platform.engine.errorhandling.errorcode.ErrorCode;
import org.qubership.integration.platform.engine.events.ConsulSessionCreatedEvent;
import org.qubership.integration.platform.engine.forms.FormData;
import org.qubership.integration.platform.engine.model.RuntimeIntegrationCache;
import org.qubership.integration.platform.engine.model.constants.CamelConstants.ChainProperties;
import org.qubership.integration.platform.engine.model.deployment.DeploymentOperation;
import org.qubership.integration.platform.engine.model.deployment.engine.DeploymentStatus;
import org.qubership.integration.platform.engine.model.deployment.engine.EngineDeployment;
import org.qubership.integration.platform.engine.model.deployment.engine.EngineState;
import org.qubership.integration.platform.engine.model.deployment.properties.CamelDebuggerProperties;
import org.qubership.integration.platform.engine.model.deployment.update.*;
import org.qubership.integration.platform.engine.security.QipSecurityAccessPolicy;
import org.qubership.integration.platform.engine.service.debugger.CamelDebugger;
import org.qubership.integration.platform.engine.service.debugger.DeploymentRuntimePropertiesService;
import org.qubership.integration.platform.engine.service.debugger.metrics.MetricsStore;
import org.qubership.integration.platform.engine.service.deployment.processing.DeploymentProcessingService;
import org.qubership.integration.platform.engine.service.deployment.processing.actions.context.before.RegisterRoutesInControlPlaneAction;
import org.qubership.integration.platform.engine.service.externallibrary.ExternalLibraryGroovyShellFactory;
import org.qubership.integration.platform.engine.service.externallibrary.ExternalLibraryService;
import org.qubership.integration.platform.engine.service.externallibrary.GroovyLanguageWithResettableCache;
import org.qubership.integration.platform.engine.service.xmlpreprocessor.XmlConfigurationPreProcessor;
import org.qubership.integration.platform.engine.util.MDCUtil;
import org.qubership.integration.platform.engine.util.log.ExtendedErrorLogger;
import org.qubership.integration.platform.engine.util.log.ExtendedErrorLoggerFactory;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.camel.xml.jaxb.JaxbHelper.loadRoutesDefinition;

@Service
public class IntegrationRuntimeService implements ApplicationContextAware {
    @SuppressWarnings("checkstyle:ConstantName")
    private static final ExtendedErrorLogger log = ExtendedErrorLoggerFactory.getLogger(IntegrationRuntimeService.class);

    private final ServerConfiguration serverConfiguration;
    private final QuartzSchedulerService quartzSchedulerService;
    private final TracingConfiguration tracingConfiguration;
    private final ExternalLibraryGroovyShellFactory groovyShellFactory;
    private final GroovyLanguageWithResettableCache groovyLanguage;
    private final MetricsStore metricsStore;
    private final Optional<ExternalLibraryService> externalLibraryService;
    private final Optional<MaasService> maasService;
    private final Optional<XmlConfigurationPreProcessor> xmlPreProcessor;
    private final VariablesService variablesService;
    private final EngineStateReporter engineStateReporter;
    private final DeploymentRuntimePropertiesService propertiesService;
    private final DeploymentReadinessService deploymentReadinessService;
    private final CamelDebugger camelDebugger;
    private final FormDataConverter formDataConverter;
    private final SecurityAccessPolicyConverter securityAccessPolicyConverter;
    private final Optional<MicrometerObservationTracer> camelObservationTracer;
    private final Predicate<FilteringEntity> camelMessageHistoryFilter;

    private final RuntimeIntegrationCache deploymentCache = new RuntimeIntegrationCache();
    private final ReadWriteLock processLock = new ReentrantReadWriteLock();

    private final Executor deploymentExecutor;

    private final DeploymentProcessingService deploymentProcessingService;

    static {
        ProcessorReifier.registerReifier(StepDefinition.class, CustomStepReifier::new);
        ProcessorReifier.registerReifier(CircuitBreakerDefinition.class,
            (route, definition) -> new CustomResilienceReifier(route,
                (CircuitBreakerDefinition) definition));
    }

    @Value("${qip.camel.stream-caching.enabled}")
    private boolean enableStreamCaching;

    private final int streamCachingBufferSize;
    @Getter
    private SpringCamelContext camelContext;

    @Autowired
    public IntegrationRuntimeService(ServerConfiguration serverConfiguration,
        QuartzSchedulerService quartzSchedulerService,
        TracingConfiguration tracingConfiguration,
        ExternalLibraryGroovyShellFactory groovyShellFactory,
        GroovyLanguageWithResettableCache groovyLanguage,
        MetricsStore metricsStore,
        Optional<ExternalLibraryService> externalLibraryService,
        Optional<MaasService> maasService,
        Optional<XmlConfigurationPreProcessor> xmlPreProcessor,
        VariablesService variablesService,
        EngineStateReporter engineStateReporter,
        @Qualifier("deploymentExecutor") Executor deploymentExecutor,
        DeploymentRuntimePropertiesService propertiesService,
        @Value("${qip.camel.stream-caching.buffer.size-kb}") int streamCachingBufferSizeKb,
        Predicate<FilteringEntity> camelMessageHistoryFilter,
        DeploymentReadinessService deploymentReadinessService,
        DeploymentProcessingService deploymentProcessingService,
        CamelDebugger camelDebugger,
        FormDataConverter formDataConverter,
        SecurityAccessPolicyConverter securityAccessPolicyConverter,
        Optional<MicrometerObservationTracer> camelObservationTracer
    ) {
        this.serverConfiguration = serverConfiguration;
        this.quartzSchedulerService = quartzSchedulerService;
        this.tracingConfiguration = tracingConfiguration;
        this.groovyShellFactory = groovyShellFactory;
        this.groovyLanguage = groovyLanguage;
        this.metricsStore = metricsStore;
        this.externalLibraryService = externalLibraryService;
        this.maasService = maasService;
        this.xmlPreProcessor = xmlPreProcessor;
        this.variablesService = variablesService;
        this.engineStateReporter = engineStateReporter;
        this.deploymentExecutor = deploymentExecutor;
        this.propertiesService = propertiesService;

        this.streamCachingBufferSize = streamCachingBufferSizeKb * 1024;

        this.camelMessageHistoryFilter = camelMessageHistoryFilter;

        this.deploymentReadinessService = deploymentReadinessService;
        this.deploymentProcessingService = deploymentProcessingService;
        this.camelDebugger = camelDebugger;
        this.formDataConverter = formDataConverter;
        this.securityAccessPolicyConverter = securityAccessPolicyConverter;
        this.camelObservationTracer = camelObservationTracer;

    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        this.camelContext = buildContext(applicationContext);
    }

    @Async
    @EventListener
    public void onExternalLibrariesUpdated(ConsulSessionCreatedEvent event) {
        // if consul session (re)create - force update engine state
        updateEngineState();
    }

    // requires completion of all deployment processes
    public List<DeploymentInfo> buildExcludeDeploymentsMap() {
        Lock processLock = this.processLock.writeLock();
        try {
            processLock.lock();
            return deploymentCache.getDeployments().values().stream()
                .map(EngineDeployment::getDeploymentInfo)
                .toList();
        } finally {
            processLock.unlock();
        }
    }

    // requires completion of all deployment processes
    private Map<String, EngineDeployment> buildActualDeploymentsSnapshot() {
        Lock processLock = this.processLock.writeLock();
        try {
            processLock.lock();
            // copy deployments objects to avoid concurrent modification
            return deploymentCache.getDeployments().entrySet().stream()
                .collect(Collectors.toMap(
                    Entry::getKey, entry -> entry.getValue().toBuilder().build()));
        } finally {
            processLock.unlock();
        }
    }

    /**
     * Start parallel deployments processing, wait for completion and update engine state
     */
    public void processAndUpdateState(DeploymentsUpdate update, boolean retry)
        throws ExecutionException, InterruptedException {
        List<CompletableFuture<?>> completableFutures = new ArrayList<>();

        // <chainId, OrderedCollection<DeploymentUpdate>>
        Map<String, TreeSet<DeploymentUpdate>> updatesPerChain = new HashMap<>();
        for (DeploymentUpdate deploymentUpdate : update.getUpdate()) {
            // deployments with same chainId must be ordered
            TreeSet<DeploymentUpdate> chainDeployments = updatesPerChain.computeIfAbsent(
                deploymentUpdate.getDeploymentInfo().getChainId(),
                k -> new TreeSet<>(
                    Comparator.comparingLong(d -> d.getDeploymentInfo().getCreatedWhen())));

            chainDeployments.add(deploymentUpdate);
        }

        for (Entry<String, TreeSet<DeploymentUpdate>> entry : updatesPerChain.entrySet()) {
            TreeSet<DeploymentUpdate> chainDeployments = entry.getValue();
            completableFutures.add(process(chainDeployments, DeploymentOperation.UPDATE, retry));
        }

        for (DeploymentUpdate toStop : update.getStop()) {
            completableFutures.add(
                process(Collections.singletonList(toStop), DeploymentOperation.STOP, retry));
        }

        // wait for all async tasks
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).get();

        // update engine state in consul
        updateEngineState();
    }

    private synchronized void updateEngineState() {
        engineStateReporter.addStateToQueue(EngineState.builder()
            .engine(serverConfiguration.getEngineInfo())
            .deployments(buildActualDeploymentsSnapshot())
            .build());
    }

    /**
     * @param chainDeployments - an ordered collection of deployments related to the same chain
     * @param operation        - operation type
     */
    private CompletableFuture<?> process(Collection<DeploymentUpdate> chainDeployments,
        DeploymentOperation operation, boolean retry) {
        return CompletableFuture.runAsync(() -> {
            MDCUtil.setRequestId(UUID.randomUUID().toString());
            for (DeploymentUpdate chainDeployment : chainDeployments) {
                log.info("Start processing deployment {}, operation: {}",
                    chainDeployment.getDeploymentInfo(), operation);

                Lock chainLock = getCache().getLockForChain(
                    chainDeployment.getDeploymentInfo().getChainId());
                try {
                    chainLock.lock();
                    log.debug("Locked by-chain lock");

                    // update and retry concurrent case: check if retry deployment is still required
                    // necessary so as not to break the order of deployments
                    if (!retry || getCache().getDeployments()
                        .containsKey(chainDeployment.getDeploymentInfo().getDeploymentId())) {
                        Lock processWeakLock = processLock.readLock();
                        try {
                            processWeakLock.lock();
                            log.debug("Locked process read lock");
                            processDeploymentUpdate(chainDeployment, operation);
                        } finally {
                            processWeakLock.unlock();
                            log.debug("Unlocked process read lock");
                        }
                    }
                } finally {
                    chainLock.unlock();
                    log.debug("Unlocked by-chain lock");
                }
                log.info("Deployment {} processing completed",
                    chainDeployment.getDeploymentInfo().getDeploymentId());
            }
        }, deploymentExecutor);
    }

    private void processDeploymentUpdate(DeploymentUpdate deployment,
                                         DeploymentOperation operation) {
        String chainId = deployment.getDeploymentInfo().getChainId();
        String snapshotId = deployment.getDeploymentInfo().getSnapshotId();
        String deploymentId = deployment.getDeploymentInfo().getDeploymentId();
        Throwable exception = null;
        ErrorCode chainErrorCode = null;
        DeploymentStatus status = DeploymentStatus.FAILED;

        try {
            MDCUtil.setBusinessIds(Map.of(
                BusinessIds.CHAIN_ID, chainId,
                BusinessIds.DEPLOYMENT_ID, deploymentId,
                BusinessIds.SNAPSHOT_ID, snapshotId));

            log.info("Processing deployment {}: {} for chain {}", deploymentId, deployment.getDeploymentInfo(), chainId);

            status = processDeployment(deployment, operation);
        } catch (KubeApiException e) {
            exception = e;
        } catch (DeploymentRetriableException e) {
            status = DeploymentStatus.PROCESSING;
            putInRetryQueue(deployment);
            exception = e;
            chainErrorCode = ErrorCode.PREDEPLOY_CHECK_ERROR;
        } catch (Throwable e) {
            exception = e;
            chainErrorCode = ErrorCode.UNEXPECTED_DEPLOYMENT_ERROR;
            log.error(chainErrorCode, chainErrorCode.compileMessage(deploymentId), e);
        } finally {
            try {
                log.info("Status of deployment {} for chain {} is {}", deploymentId, chainId, status);
                quartzSchedulerService.resetSchedulersProxy();
                switch (status) {
                    case DEPLOYED, FAILED, PROCESSING -> {
                        if (chainErrorCode != null) {
                            deployment.getDeploymentInfo().setChainStatusCode(chainErrorCode.getCode());
                        }
                        var stateBuilder = EngineDeployment.builder()
                                .deploymentInfo(deployment.getDeploymentInfo())
                                .status(status);

                        if (isNull(exception)) {
                            removeOldDeployments(deployment, deploymentStatus -> true);
                        } else {
                            stateBuilder.errorMessage(exception.getMessage());

                            log.error(chainErrorCode, "Failed to deploy chain {} with id {}. Deployment: {}",
                                    deployment.getDeploymentInfo().getChainName(), chainId, deploymentId, exception);

                            removeOldDeployments(
                                    deployment,
                                    (deploymentStatus) ->
                                            deploymentStatus == DeploymentStatus.FAILED
                                                    || deploymentStatus == DeploymentStatus.PROCESSING);
                        }

                        // If Pod is not initialized yet and this is first deploy -
                        // set corresponding flag and Processing status
                        if (status == DeploymentStatus.DEPLOYED && isDeploymentsSuspended()) {
                            stateBuilder.suspended(true);
                        }

                        EngineDeployment deploymentState = stateBuilder.build();
                        getCache().getDeployments().put(deploymentId, deploymentState);
                    }
                    case REMOVED -> {
                        getCache().getDeployments().remove(deploymentId);
                        getCache().cleanForDeployment(deploymentId);
                        removeRetryingDeployment(deploymentId);
                        propertiesService.removeDeployProperties(deploymentId);
                        metricsStore.removeChainsDeployments(deploymentId);
                    }
                    default -> {
                    }
                }
            } finally {
                MDCUtil.clear();
            }
        }
    }

    private boolean isDeploymentsSuspended() {
        return !deploymentReadinessService.isInitialized();
    }

    /**
     * Method, which process provided configuration and returns occurred exception
     */
    private DeploymentStatus processDeployment(
        DeploymentUpdate deployment,
        DeploymentOperation operation
    ) throws Exception {
        return switch (operation) {
            case UPDATE -> update(deployment);
            case STOP -> stop(deployment.getDeploymentInfo());
        };
    }

    private DeploymentStatus update(DeploymentUpdate deployment) throws Exception {
        DeploymentInfo deploymentInfo = deployment.getDeploymentInfo();
        String deploymentId = deploymentInfo.getDeploymentId();
        DeploymentConfiguration configuration = deployment.getConfiguration();
        String configurationXml = preprocessDeploymentConfigurationXml(configuration);

        deploymentProcessingService.processBeforeContextCreated(deploymentInfo, configuration);

        propertiesService.mergeWithRuntimeProperties(CamelDebuggerProperties.builder()
            .deploymentInfo(deployment.getDeploymentInfo())
            .maskedFields(deployment.getMaskedFields())
            .properties(configuration.getProperties())
            .build());

        List<RouteDefinition> deployedRoutes = enrichContextWithDeployment(deploymentInfo, configuration, configurationXml);

        List<DeploymentInfo> deploymentIdsToStop = getDeploymentsRelatedToDeployment(
            deployment,
            state -> !state.getDeploymentInfo().getDeploymentId()
                .equals(deployment.getDeploymentInfo().getDeploymentId())
        );

        try {
            if (deploymentReadinessService.isInitialized()) {
                startRoutes(deployedRoutes, deploymentInfo.getDeploymentId());
            }
        } catch (Exception e) {
            quartzSchedulerService.commitScheduledJobs();
            deploymentProcessingService.processStopContext(camelContext, deploymentInfo, configuration);
            getCache().cleanForDeployment(deploymentInfo.getDeploymentId());
            refreshClassResolver();
            throw e;
        }

        deploymentIdsToStop.forEach(this::stopDeploymentContext);

        quartzSchedulerService.commitScheduledJobs();
        if (log.isDebugEnabled()) {
            log.debug("Deployment {} has started", deploymentId);
        }
        return DeploymentStatus.DEPLOYED;
    }

    private String preprocessDeploymentConfigurationXml(DeploymentConfiguration configuration) throws URISyntaxException {
        String configurationXml = configuration.getXml();

        configurationXml = variablesService.injectVariables(configurationXml, true);
        if (maasService.isPresent()) {
            configurationXml = maasService.get().resolveDeploymentMaasParameters(configuration, configurationXml);
        }
        configurationXml = resolveRouteVariables(configuration.getRoutes(), configurationXml);
        if (xmlPreProcessor.isPresent()) {
            configurationXml = xmlPreProcessor.get().process(configurationXml);
        }

        return configurationXml;
    }

    private String resolveRouteVariables(List<DeploymentRouteUpdate> routes, String text) {
        String result = text;

        for (DeploymentRouteUpdate route : routes) {
            DeploymentRouteUpdate tempRoute =
                    RegisterRoutesInControlPlaneAction.formatServiceRoutes(route);

            RouteType type = tempRoute.getType();
            if (nonNull(tempRoute.getVariableName())
                && (RouteType.EXTERNAL_SENDER == type || RouteType.EXTERNAL_SERVICE == type)) {
                String variablePlaceholder = String.format("%%%%{%s}", tempRoute.getVariableName());
                String gatewayPrefix = tempRoute.getGatewayPrefix();
                result = result.replace(variablePlaceholder,
                    isNull(gatewayPrefix) ? "" : gatewayPrefix);
            }
        }
        return result;
    }

    /**
     * Stop and remove same old deployments and contexts
     */
    private void removeOldDeployments(
        DeploymentUpdate deployment,
        Predicate<DeploymentStatus> statusPredicate
    ) {
        Iterator<Map.Entry<String, EngineDeployment>> iterator = getCache().getDeployments()
            .entrySet().iterator();
        List<DeploymentInfo> deploymentContextsToRemove = new ArrayList<>();
        while (iterator.hasNext()) {
            Map.Entry<String, EngineDeployment> entry = iterator.next();

            DeploymentInfo depInfo = entry.getValue().getDeploymentInfo();
            if (depInfo.getChainId().equals(deployment.getDeploymentInfo().getChainId())
                    && statusPredicate.test(entry.getValue().getStatus())
                    && !depInfo.getDeploymentId().equals(deployment.getDeploymentInfo().getDeploymentId())) {

                deploymentContextsToRemove.add(depInfo);

                removeRetryingDeployment(depInfo.getDeploymentId());

                metricsStore.removeChainsDeployments(depInfo.getDeploymentId());

                iterator.remove();
                propertiesService.removeDeployProperties(entry.getKey());
            }
        }

        deploymentContextsToRemove.forEach(this::stopDeploymentContext);
    }

    private List<DeploymentInfo> getDeploymentsRelatedToDeployment(
        DeploymentUpdate deployment,
        Predicate<EngineDeployment> filter
    ) {
        return getCache().getDeployments().entrySet().stream()
            .filter(entry -> entry.getValue().getDeploymentInfo().getChainId()
                .equals(deployment.getDeploymentInfo().getChainId())
                && filter.test(entry.getValue()))
            .map(Entry::getValue)
            .map(EngineDeployment::getDeploymentInfo)
            .toList();
    }

    private SpringCamelContext buildContext(ApplicationContext applicationContext) {
        SpringCamelContext context = new SpringCamelContext(applicationContext);

        context.getTypeConverterRegistry().addTypeConverter(
                FormData.class,
                String.class,
                formDataConverter);
        context.getTypeConverterRegistry().addTypeConverter(
                QipSecurityAccessPolicy.class,
                String.class,
                securityAccessPolicyConverter);

        context.getGlobalOptions().put(JacksonConstants.ENABLE_TYPE_CONVERTER, "true");
        context.getGlobalOptions().put(JacksonConstants.TYPE_CONVERTER_TO_POJO, "true");
        context.getInflightRepository().setInflightBrowseEnabled(true);

        context.setManagementName("camel-context_singleton");
        context.setManagementStrategy(new DefaultManagementStrategy(context));
        camelDebugger.stop();
        context.setDebugger(camelDebugger);
        camelDebugger.setCamelContext(context);
        context.setDebugging(true);

        configureMessageHistoryFactory(context);

        context.setStreamCaching(enableStreamCaching);
        if (enableStreamCaching) {
            DefaultStreamCachingStrategy streamCachingStrategy = new DefaultStreamCachingStrategy();
            streamCachingStrategy.setBufferSize(streamCachingBufferSize);
            context.setStreamCachingStrategy(streamCachingStrategy);
        }

        if (tracingConfiguration.isTracingEnabled()) {
            Tracer tracer = camelObservationTracer.orElseThrow(() ->
                    new UnsatisfiedDependencyException((String) null, (String) null, "camelObservationTracer", (String) null));
            tracer.init(context);
        }

        return context;
    }

    private List<RouteDefinition> enrichContextWithDeployment(
        DeploymentInfo deploymentInfo,
        DeploymentConfiguration deploymentConfiguration,
        String configurationXml
    ) throws Exception {
        enrichClassResolver(deploymentInfo.getDeploymentId(), deploymentConfiguration);
        refreshClassResolver();

        String deploymentId = deploymentInfo.getDeploymentId();

        deploymentProcessingService.processAfterContextCreated(camelContext, deploymentInfo, deploymentConfiguration);

        return loadRoutes(configurationXml, deploymentId);
    }

    private void refreshClassResolver() {
        List<String> systemModelIds = getCache().getDeploymentSystemModelIds().values().stream().flatMap(List::stream).toList();
        ClassLoader classLoader = externalLibraryService.isPresent()
                ? externalLibraryService.get().getClassLoaderForSystemModels(systemModelIds, getClass().getClassLoader())
                : getClass().getClassLoader();
        camelContext.setClassResolver(new QipCustomClassResolver(classLoader));
    }

    private void enrichClassResolver(
            String deploymentId,
            DeploymentConfiguration deploymentConfiguration
    ) {
        List<String> systemModelIds = deploymentConfiguration.getProperties().stream()
            .map(ElementProperties::getProperties)
            .filter(properties -> ChainProperties.SERVICE_CALL_ELEMENT.equals(properties.get(
                ChainProperties.ELEMENT_TYPE)))
            .map(properties -> properties.get(ChainProperties.OPERATION_SPECIFICATION_ID))
            .filter(Objects::nonNull)
            .toList();
        getCache().getDeploymentSystemModelIds().put(deploymentId, systemModelIds);
    }

    private void startRoutes(List<RouteDefinition> routes, String deploymentId) {
        if (routes == null) {
            return;
        }
        routes.forEach(route -> {
            try {
                camelContext.startRoute(route.getId());
            } catch (Exception e) {
                try {
                    camelContext.removeRouteDefinitions(routes);
                } catch (Exception ignored) {
                    // We did our best to remove bad route definitions
                }
                log.error("Unable to start routes for deployment {}", deploymentId, e);
                throw new RuntimeException("Unable to start routes for deployment " + deploymentId, e);
            }
        });
    }

    private void configureMessageHistoryFactory(SpringCamelContext context) {
        context.setMessageHistory(true);
        MessageHistoryFactory defaultFactory = context.getMessageHistoryFactory();
        FilteringMessageHistoryFactory factory = new FilteringMessageHistoryFactory(
            camelMessageHistoryFilter, defaultFactory);
        context.setMessageHistoryFactory(factory);
    }

    /**
     * Upload routes to a new context from provided configuration
     */
    private List<RouteDefinition> loadRoutes(String xmlConfiguration, String deploymentId) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Loading routes from: \n{}", xmlConfiguration);
        }

        byte[] configurationBytes = xmlConfiguration.getBytes();
        ByteArrayInputStream configInputStream = new ByteArrayInputStream(configurationBytes);
        List<RouteDefinition> routesDefinition = loadRoutesDefinition(camelContext, configInputStream).getRoutes();

        // xml routes must be marked as un-prepared as camel-core
        // must do special handling for XML DSL
        for (RouteDefinition route : routesDefinition) {
            RouteDefinitionHelper.prepareRoute(camelContext, route);
            route.markPrepared();
        }
        routesDefinition.forEach(route -> {
            route.markUnprepared();
            route.setRouteProperties(List.of(new PropertyDefinition(ChainProperties.DEPLOYMENT_ID, deploymentId)));
        });

        compileGroovyScripts(routesDefinition);

        camelContext.addRouteDefinitions(routesDefinition);
        getCache().getDeploymentRoutes().put(deploymentId, routesDefinition);
        return routesDefinition;
    }

    private void compileGroovyScripts(List<RouteDefinition> routesDefinition) {
        for (RouteDefinition route : routesDefinition) {
            for (ProcessorDefinition<?> processor : route.getOutputs()) {
                if (!(processor instanceof ExpressionNode)) {
                    continue;
                }
                ExpressionDefinition expression = ((ExpressionNode) processor).getExpression();
                if (!expression.getLanguage().equals("groovy")) {
                    continue;
                }

                log.debug("Compiling groovy script for processor {}", processor.getId());
                compileGroovyScript(expression);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void compileGroovyScript(ExpressionDefinition expression) {
        try {
            String text = expression.getExpression();
            if (isNull(expression.getTrim()) || Boolean.parseBoolean(expression.getTrim())) {
                text = text.trim();
            }

            GroovyShell groovyShell = groovyShellFactory.createGroovyShell(null);
            Class<Script> scriptClass = groovyShell.getClassLoader().parseClass(text);
            groovyLanguage.addScriptToCache(text, scriptClass);
        } catch (CompilationFailedException exception) {
            if (isClassResolveError(exception)) {
                throw new DeploymentRetriableException("Failed to compile groovy script.",
                    exception);
            } else {
                throw new RuntimeException("Failed to compile groovy script.", exception);
            }
        }
    }

    private static boolean isClassResolveError(CompilationFailedException exception) {
        return exception.getMessage().contains("unable to resolve class");
    }

    private DeploymentStatus stop(DeploymentInfo deploymentInfo) {
        stopDeploymentContext(deploymentInfo);
        return DeploymentStatus.REMOVED;
    }

    private void stopDeploymentContext(DeploymentInfo deploymentInfo) {
        List<RouteDefinition> routeDefinitions = getCache().getDeploymentRoutes().get(deploymentInfo.getDeploymentId());
        if (CollectionUtils.isEmpty(routeDefinitions)) {
            return;
        }

        deploymentProcessingService.processStopContext(camelContext, deploymentInfo, null);
        quartzSchedulerService.removeSchedulerJobsFromRoutes(routeDefinitions, camelContext);
        try {
            camelContext.removeRouteDefinitions(routeDefinitions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getCache().cleanForDeployment(deploymentInfo.getDeploymentId());
        refreshClassResolver();
    }

    public void retryProcessingDeploys() {
        try {
            Collection<DeploymentUpdate> toRetry = getCache().flushDeploymentsToRetry();
            if (!toRetry.isEmpty()) {
                processAndUpdateState(DeploymentsUpdate.builder().update(toRetry).build(), true);
            }
        } catch (Exception e) {
            log.error("Failed to process retry deployments", e);
        }
    }

    private void putInRetryQueue(DeploymentUpdate deploymentUpdate) {
        log.info("Deployment marked for retry {}",
            deploymentUpdate.getDeploymentInfo().getDeploymentId());
        getCache().putToRetryQueue(deploymentUpdate);
    }

    private void removeRetryingDeployment(String deploymentId) {
        getCache().removeRetryDeploymentFromQueue(deploymentId);
    }

    public RuntimeIntegrationCache getCache() {
        return deploymentCache;
    }

    public void startAllRoutesOnInit() {
        while (true) {
            try {
                camelContext.start();
                log.debug("CamelContext has been started");
                getCache().getDeployments().entrySet().stream().forEach(entry -> {
                    EngineDeployment state = entry.getValue();
                    if (state != null) {
                        state.setSuspended(false);
                    }
                });
                break;
            } catch (FailedToCreateRouteException | FailedToStartRouteException e) {
                String routeId = e instanceof FailedToCreateRouteException
                        ? ((FailedToCreateRouteException) e).getRouteId() : ((FailedToStartRouteException) e).getRouteId();
                Optional<String> deploymentId = getCache().getDeploymentIdByRouteId(routeId);
                List<RouteDefinition> routesToStop = deploymentId.map(id -> getCache().getDeploymentRoutes().get(id)).orElse(null);
                if (CollectionUtils.isEmpty(routesToStop)) {
                    throw new RuntimeException(String.format("Failed to find routes for deploymentId %s to stop error deployment at application startup, error route is %s",
                            deploymentId.orElse("null"), routeId), e);
                }
                log.error("Failed to start deployment {} during startup ", deploymentId.get(), e);
                EngineDeployment state = getCache().getDeployments().get(deploymentId.get());
                if (state != null) {
                    state.setStatus(DeploymentStatus.FAILED);
                    state.setErrorMessage(e.getMessage());
                }
                try {
                    camelContext.removeRouteDefinitions(routesToStop);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } catch (Exception e) {
                log.error("CamelContext unable to startup", e);
                getCache().getDeployments().entrySet().stream().forEach(entry -> {
                    EngineDeployment state = entry.getValue();
                    if (state != null) {
                        state.setStatus(DeploymentStatus.FAILED);
                        state.setErrorMessage("All deployments failed during pod startup " + e.getMessage());
                    }
                });
                throw e;
            }
        }
    }

    private void runInProcessLock(Runnable callback) {
        Lock lock = this.processLock.writeLock();
        try {
            lock.lock();
            callback.run();
        } finally {
            lock.unlock();
        }
    }

    public void suspendAllSchedulers() {
        runInProcessLock(quartzSchedulerService::suspendAllSchedulers);
    }

    public void resumeAllSchedulers() {
        runInProcessLock(quartzSchedulerService::resumeAllSchedulers);
    }
}
