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

package org.qubership.integration.platform.engine.camel.components.directvm;

import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultAsyncProducer;
import org.apache.camel.support.task.ForegroundTask;
import org.apache.camel.support.task.Tasks;
import org.apache.camel.support.task.budget.Budgets;
import org.apache.camel.support.task.budget.IterationBoundedBudget;
import org.apache.camel.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;

/**
 * The direct producer.
 * <p/>
 * If blocking is enabled ({@code DirectEndpoint#isBlock}) then the DirectEndpoint will create an instance of this class
 * instead of {@code DirectProducer}. This producers {@code process} method will block for the configured duration
 * ({@code DirectEndpoint#getTimeout}, default to 30 seconds). After which if a consumer is still unavailable a
 * DirectConsumerNotAvailableException will be thrown.
 * <p/>
 * Implementation note: Concurrent Producers will block for the duration it takes to determine if a consumer is
 * available, but actual consumer execution will happen concurrently.
 */
public class ChainBlockingProducer extends DefaultAsyncProducer {

    private static final Logger LOG = LoggerFactory.getLogger(ChainBlockingProducer.class);

    private final ChainEndpoint endpoint;

    public ChainBlockingProducer(ChainEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        getConsumer(exchange).getProcessor().process(exchange);
    }

    @Override
    public boolean process(Exchange exchange, AsyncCallback callback) {
        try {
            return getConsumer(exchange).getAsyncProcessor().process(exchange, callback);
        } catch (Exception e) {
            exchange.setException(e);
            callback.done(true);
            return true;
        }
    }

    protected ChainConsumer getConsumer(Exchange exchange) throws Exception {
        ChainConsumer answer = endpoint.getConsumer();
        if (answer == null) {
            // okay then await until we have a consumer or we timed out
            if (endpoint.isFailIfNoConsumers()) {
                throw new ChainConsumerNotAvailableException("No consumers available on endpoint: " + endpoint, exchange);
            } else {
                answer = awaitConsumer();
                if (answer == null) {
                    throw new ChainConsumerNotAvailableException(
                            "No consumers available on endpoint: " + endpoint, exchange);
                }
            }
        }

        return answer;
    }

    private ChainConsumer awaitConsumer() {
        ForegroundTask task = Tasks.foregroundTask().withBudget(Budgets.iterationTimeBudget()
                .withMaxIterations(IterationBoundedBudget.UNLIMITED_ITERATIONS)
                .withMaxDuration(Duration.ofMillis(endpoint.getTimeout()))
                .withInterval(Duration.ofMillis(500))
                .build())
                .build();

        StopWatch watch = new StopWatch();
        ChainConsumer answer = task.run(endpoint::getConsumer, Objects::nonNull)
                .orElse(null);
        LOG.debug("Waited {} for consumer to be ready", watch.taken());

        return answer;
    }

}
