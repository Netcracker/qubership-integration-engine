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

package org.qubership.integration.platform.engine.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionHandler {

    @Transactional(value = "checkpointTransactionManager", propagation = Propagation.REQUIRED)
    public void runInCheckpointTransaction(Runnable callback) {
        callback.run();
    }

    @Transactional(value = "checkpointTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void runInNewCheckpointTransaction(Runnable callback) {
        callback.run();
    }

    @Transactional(value = "schedulerTransactionManager", propagation = Propagation.REQUIRED)
    public void runInSchedulerTransaction(Runnable callback) {
        callback.run();
    }

    @Transactional(value = "schedulerTransactionManager", propagation = Propagation.REQUIRES_NEW)
    public void runInNewSchedulerTransaction(Runnable callback) {
        callback.run();
    }
}