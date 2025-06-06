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

package org.qubership.integration.platform.engine;

import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.qubership.integration.platform.engine.opensearch.ism.converters.OpenSearchTypeConvertersRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = CamelAutoConfiguration.class)
public class IntegrationEngineApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(IntegrationEngineApplication.class);
        application.addListeners(new OpenSearchTypeConvertersRegistrar());
        application.run(args);
    }
}
