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

package org.qubership.integration.platform.engine.configuration;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class MetricsFilterConfiguration {
    @Produces
    public MeterFilter meterFilter() {
        return new MeterFilter() {
            @Override
            public Meter.@NotNull Id map(Meter.@NotNull Id id) {
                if (id.getName().startsWith("kafka")) {
                    List<Tag> tags = id.getTags().stream()
                            .filter(t -> !("client.id".equals(t.getKey()) || "node.id".equals(t.getKey())))
                            .collect(Collectors.toList());
                    return id.replaceTags(tags);
                }
                return id;
            }
        };
    }
}
