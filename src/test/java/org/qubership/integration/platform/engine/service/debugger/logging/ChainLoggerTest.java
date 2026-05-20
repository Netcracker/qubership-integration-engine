package org.qubership.integration.platform.engine.service.debugger.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.integration.platform.engine.model.SessionElementProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChainLoggerTest {

    private ChainLogger chainLogger;

    @BeforeEach
    void setUp() {
        chainLogger = new ChainLogger(null, Optional.empty());
    }

    @Test
    void shouldFilterSensitiveExchangeProperties() {
        Map<String, SessionElementProperty> props = new HashMap<>();
        props.put("dbaas_password", new SessionElementProperty("java.lang.String", "secret"));
        props.put("dbaas_username", new SessionElementProperty("java.lang.String", "secret"));
        props.put("namespace", new SessionElementProperty("java.lang.String", "qa08"));
        Map<String, SessionElementProperty> filteredExchangeProperties = chainLogger.filterExchangeProperties(props);
        assertFalse(filteredExchangeProperties.containsKey("dbaas_password"));
        assertFalse(filteredExchangeProperties.containsKey("dbaas_username"));
        assertTrue(filteredExchangeProperties.containsKey("namespace"));
    }
}
