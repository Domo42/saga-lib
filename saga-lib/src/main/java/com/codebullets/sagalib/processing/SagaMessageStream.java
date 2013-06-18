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
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.storage.StateStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

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
    public SagaMessageStream(final SagaFactory sagaFactory, final HandlerInvoker invoker, StateStorage storage) {
        this.sagaFactory = sagaFactory;
        this.invoker = invoker;
        this.storage = storage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Object message) {
    }

    @Override
    public void handle(final Object message) throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handleSagaMessage(message);
    }

    /**
     * Perform handling of a single message.
     */
    private void handleSagaMessage(final Object message) throws InvocationTargetException, IllegalAccessException {
        Collection<Saga> sagas = sagaFactory.create(message);
        if (sagas.isEmpty()) {
            LOG.warn("No saga found to handle message. {}", message);
        }
        else {
            for (Saga saga : sagas) {
                invoker.invoke(saga, message);
                updateStateStorage(saga);
            }
        }
    }

    /**
     * Updates the state storage depending on whether the saga is completed or keeps on running.
     */
    private void updateStateStorage(Saga saga) {
        if (saga.isCompleted()) {
            storage.delete(saga.state().getSagaId());
        } else {
            storage.save(saga.state());
        }
    }
}
