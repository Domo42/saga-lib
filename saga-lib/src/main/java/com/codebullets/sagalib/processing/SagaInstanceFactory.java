/*
 * Copyright 2015 Stefan Domnanovits
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
import com.codebullets.sagalib.SagaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Create a new saga instance. This means creating the actual instance
 * as well as applying initializing certain saga parameters.
 */
public class SagaInstanceFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SagaInstanceFactory.class);

    private final SagaInstanceCreator creator;

    /**
     * Generates a new instance of SagaInstanceFactory.
     */
    @Inject
    public SagaInstanceFactory(final SagaInstanceCreator creator) {
        this.creator = creator;
    }

    /**
     * Creates and initializes a new saga instance based on the provided type information.
     */
    public SagaInstanceInfo createNew(final SagaType type) {
        Saga newSaga = startNewSaga(type.getSagaClass());
        return SagaInstanceInfo.define(newSaga, true);
    }

    /**
     * Starts a new saga by creating an instance and attaching a new saga state.
     */
    private Saga startNewSaga(final Class<? extends Saga> sagaToStart) {
        Saga createdSaga = null;

        try {
            createdSaga = createNewSagaInstance(sagaToStart);
            createdSaga.createNewState();

            SagaState newState = createdSaga.state();
            newState.setSagaId(UUID.randomUUID().toString());
            newState.setType(sagaToStart.getName());
        } catch (Exception ex) {
            LOG.error("Unable to create new instance of saga type {}.", sagaToStart, ex);
        }

        return createdSaga;
    }

    /**
     * Creates a new saga instance attaching an existing saga state.
     * @throws ExecutionException Is thrown in case no provider can be found to create an instance.
     *                            The actual cause can be inspected by {@link ExecutionException#getCause()}.
     */
    public Saga continueExisting(final Class<? extends Saga> sagaType, final SagaState state) throws ExecutionException {
        // do not catch exception as in create new
        // if there is an exception during creation, although we want to continue
        // something is terribly wrong -> it has worked at least once before.
        Saga saga = createNewSagaInstance(sagaType);
        saga.setState(state);
        return saga;
    }

    /**
     * Create a new saga instance based on fully qualified name and the existing saga state.
     * @throws ExecutionException Is thrown in case no provider can be found to create an instance.
     *                            The actual cause can be inspected by {@link ExecutionException#getCause()}.
     */
    public Saga continueExisting(final String sagaTypeName, final SagaState state) throws ExecutionException {
        return continueSaga(sagaTypeName, state);
    }

    private Saga continueSaga(final String sagaToContinue, final SagaState existingState) {
        Saga saga = null;

        try {
            Class clazz = Class.forName(sagaToContinue);
            saga = continueExisting(clazz, existingState);
        } catch (ClassNotFoundException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return saga;
    }

    private Saga createNewSagaInstance(final Class<? extends Saga> sagaType) throws ExecutionException {
        return creator.createNew(sagaType);
    }
}