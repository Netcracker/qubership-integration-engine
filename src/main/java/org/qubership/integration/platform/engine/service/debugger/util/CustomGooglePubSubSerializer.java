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

package org.qubership.integration.platform.engine.service.debugger.util;

import org.apache.camel.component.google.pubsub.serializer.GooglePubsubSerializer;
import org.apache.camel.converter.stream.InputStreamCache;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

@ApplicationScoped
public class CustomGooglePubSubSerializer implements GooglePubsubSerializer {

    @Override
    public byte[] serialize(Object payload) throws IOException {
        if (payload instanceof InputStreamCache) {
            return ((InputStreamCache) payload).readAllBytes();
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(payload);
            return out.toByteArray();
        }
    }
}
