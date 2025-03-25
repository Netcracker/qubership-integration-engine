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

package org.qubership.integration.platform.engine.camel.context.propagation;

import java.util.Map;
import java.util.function.Function;

public interface CamelExchangeContextPropagation {
    void initRequestContext(Map<String, Object> headers);

    Map<String, Object> createContextSnapshot();

    Map<String, String> buildContextSnapshotForSessions();

    <T, R> R getSafeContext(String contextName, Function<T, R> getter);

    void activateContextSnapshot(Map<String, Object> snapshot);

    void propagateExchangeHeaders(Map<String, Object> headers);

    void propagateHeaders(Map<String, String> headers);

    void removeContextHeaders(Map<String, Object> exchangeHeaders);

    void clear();
}
