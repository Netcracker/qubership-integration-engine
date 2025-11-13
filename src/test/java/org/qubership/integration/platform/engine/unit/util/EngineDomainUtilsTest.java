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

package org.qubership.integration.platform.engine.unit.util;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EngineDomainUtilsPlainTest {
//
//    private static SmallRyeConfig cfg;
//    private static final ClassLoader CL =
//            Thread.currentThread().getContextClassLoader();
//
//    @BeforeAll
//    static void setUpConfig() {
//        Properties props = new Properties();
//        props.setProperty("application.default_integration_domain_name", "default");
//        props.setProperty("application.default_integration_domain_microservice_name", "qip-engine");
//
//        cfg = new SmallRyeConfigBuilder()
//                .withSources(new PropertiesConfigSource(props, "test-props", 1000))
//                .build();
//
//        ConfigProviderResolver.instance().registerConfig(cfg, CL);
//    }
//
//    @AfterAll
//    static void tearDownConfig() {
//        ConfigProviderResolver.instance().releaseConfig(cfg);
//    }
//
//    @Test
//    void should_return_default_domain_when_microservice_name_matches_configured_default() {
//        EngineDomainUtils utils = new EngineDomainUtils(); // ок, если нет CDI-инъекций
//        assertEquals("default", utils.extractEngineDomain("qip-engine"));
//    }
//
//    @Test
//    void should_return_input_name_when_microservice_name_differs_from_configured_default() {
//        EngineDomainUtils utils = new EngineDomainUtils();
//        assertEquals("qip-custom-brand-engine", utils.extractEngineDomain("qip-custom-brand-engine"));
//    }
}
