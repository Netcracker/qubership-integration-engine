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

import org.apache.camel.Processor;
import org.apache.camel.component.servlet.ServletConsumer;
import org.apache.camel.component.servlet.ServletEndpoint;

import java.util.Date;

public class ServletCustomConsumer extends ServletConsumer {
    private final long creationTime;

    public ServletCustomConsumer(ServletEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.creationTime = new Date().getTime();
    }

    public long getCreationTime() {
        return creationTime;
    }
}
