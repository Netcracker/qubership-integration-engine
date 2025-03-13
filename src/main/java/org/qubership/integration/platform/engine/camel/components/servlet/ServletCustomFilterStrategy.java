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

package org.qubership.integration.platform.engine.camel.components.servlet;

import org.apache.camel.http.common.HttpHeaderFilterStrategy;
import org.qubership.integration.platform.engine.camel.components.context.propagation.ContextPropsProvider;

import java.util.Locale;
import java.util.Optional;

public class ServletCustomFilterStrategy extends HttpHeaderFilterStrategy {

    public ServletCustomFilterStrategy(Optional<ContextPropsProvider> propsProvider) {
        propsProvider.ifPresent(bean -> bean.getHeaderToContextMapping().keySet()
            .forEach(header -> getOutFilter().add(header.toLowerCase(Locale.ENGLISH))));
    }
}
