package org.qubership.integration.platform.engine.service.debugger.logging;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubership.integration.platform.engine.model.SessionElementProperty;
import org.qubership.integration.platform.engine.model.deployment.properties.CamelDebuggerProperties;
import org.qubership.integration.platform.engine.model.deployment.properties.DeploymentRuntimeProperties;
import org.qubership.integration.platform.engine.model.logging.LogLoggingLevel;
import org.qubership.integration.platform.engine.model.logging.LogPayload;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChainLoggerTest {

    @Spy
    @InjectMocks
    private ChainLogger chainLogger;

    @Test
    void testLogBeforeProcess() {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        CamelDebuggerProperties dbg = mock(CamelDebuggerProperties.class);
        DeploymentRuntimeProperties runtimeProperties = mock(DeploymentRuntimeProperties.class);
        LogLoggingLevel logLoggingLevel = mock(LogLoggingLevel.class);

        when(dbg.getRuntimeProperties(exchange)).thenReturn(runtimeProperties);
        when(runtimeProperties.getLogPayload()).thenReturn(Set.of(LogPayload.BODY));
        when(runtimeProperties.getLogLoggingLevel()).thenReturn(logLoggingLevel);
        when(logLoggingLevel.isInfoLevel()).thenReturn(false);

        Map<String, String> headersForLogging = new HashMap<>();
        headersForLogging.put("Authorization", "Bearer real-token");
        headersForLogging.put("X-Request-Id", "123");

        Map<String, SessionElementProperty> exchangePropertiesForLogging = new HashMap<>();
        exchangePropertiesForLogging.put("dbaas_password", new SessionElementProperty("java.lang.String", "secret"));
        exchangePropertiesForLogging.put("dbaas_username", new SessionElementProperty("java.lang.String", "secret"));
        exchangePropertiesForLogging.put("namespace", new SessionElementProperty("java.lang.String", "qa08"));

        chainLogger.logBeforeProcess(exchange, dbg, "body", headersForLogging, exchangePropertiesForLogging, "node-1");

        assertFalse(headersForLogging.containsKey("Authorization"));
        assertFalse(exchangePropertiesForLogging.containsKey("dbaas_password"));
        assertFalse(exchangePropertiesForLogging.containsKey("dbaas_username"));
        assertTrue(exchangePropertiesForLogging.containsKey("namespace"));
    }
}
