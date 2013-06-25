/*
 * Copyright 2013 Stefan Domnanovits
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.messages.Timeout;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutExpired;
import com.codebullets.sagalib.timeout.TimeoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls the saga message flow.
 */
public class SagaMessageStream implements MessageStream {
    private static final Logger LOG = LoggerFactory.getLogger(SagaFactory.class);

    private final SagaFactory sagaFactory;
    private final HandlerInvoker invoker;
    private final StateStorage storage;

    /**
     * Creates a new SagaMessageStream instance.
     */
    @Inject
    public SagaMessageStream(final SagaFactory sagaFactory, final HandlerInvoker invoker, final StateStorage storage, final TimeoutManager timeoutManager) {
        this.sagaFactory = sagaFactory;
        this.invoker = invoker;
        this.storage = storage;

        timeoutManager.addExpiredCallback(new TimeoutExpired() {
            @Override
            public void expired(final Timeout timeout) {
                timeoutHasExpired(timeout);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Object message) {
        checkNotNull(message, "Message to handle must not be null.");

        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final Object message) throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");

        SagaExecutionTask executor = createExecutor(message);
        executor.handle();
    }

    /**
     * Called whenever the timeout manager reports an expired timeout.
     */
    private void timeoutHasExpired(final Timeout timeout) {
        try {
            handle(timeout);
        } catch (Exception ex) {
            LOG.error("Error handling timeout {}", timeout, ex);
        }
    }

    private SagaExecutionTask createExecutor(final Object message) {
        return new SagaExecutionTask(sagaFactory, invoker, storage, message);
    }
}
