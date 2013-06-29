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

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Perform execution of saga message handling. This class is the execution
 * root unit when handling messages as part of an execution strategy.
 */
public class SagaExecutionTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SagaExecutionTask.class);

    private final SagaFactory sagaFactory;
    private final HandlerInvoker invoker;
    private final StateStorage storage;
    private final TimeoutManager timeoutManager;
    private final Object message;

    /**
     * Generates a new instance of SagaExecutionTask.
     */
    public SagaExecutionTask(final SagaFactory sagaFactory, final HandlerInvoker invoker, final StateStorage storage, final TimeoutManager timeoutManager,
                             final Object message) {
        this.sagaFactory = sagaFactory;
        this.invoker = invoker;
        this.storage = storage;
        this.timeoutManager = timeoutManager;
        this.message = message;
    }

    /**
     * Performs synchronous saga handling of the message provided in ctor.
     *
     * @throws InvocationTargetException Thrown when invocation of the handler method fails.
     * @throws IllegalAccessException Thrown when access to the handler method fails.
     */
    public void handle() throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handleSagaMessage(message);
    }

    /**
     * Perform handling of a single message.
     */
    private void handleSagaMessage(final Object invokeParam) throws InvocationTargetException, IllegalAccessException {
        Collection<Saga> sagas = sagaFactory.create(invokeParam);
        if (sagas.isEmpty()) {
            LOG.warn("No saga found to handle message. {}", invokeParam);
        } else {
            for (Saga saga : sagas) {
                invoker.invoke(saga, invokeParam);
                updateStateStorage(saga);
            }
        }
    }

    /**
     * Updates the state storage depending on whether the saga is completed or keeps on running.
     */
    private void updateStateStorage(final Saga saga) {
        if (saga.isCompleted()) {
            storage.delete(saga.state().getSagaId());
            timeoutManager.cancelTimeouts(saga.state().getSagaId());
        } else {
            storage.save(saga.state());
        }
    }

    /**
     * Similar to {@link #handle()} but intended for execution on any thread.<p/>
     * May throw a runtime exception in case something went wrong invoking the target saga message handler.
     */
    @Override
    public void run() {
        try {
            handle();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}