/*
 * Copyright 2016 Stefan Domnanovits
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.codebullets.sagalib;

import com.codebullets.sagalib.context.NeedContext;
import com.codebullets.sagalib.describe.DescribesHandlers;
import com.codebullets.sagalib.describe.HandlerDescription;
import com.codebullets.sagalib.describe.HandlerDescriptions;

import java.util.Collection;
import java.util.Collections;

/**
 * Inherit from this class to quickly handle any event put on the
 * message stream. This class implements a simplified saga that does
 * not have state and is automatically finished.
 *
 * <p>Compared to {@link AbstractSingleEventSaga} this class is using
 * the direct handler description feature and does not rely on annotations.</p>
 *
 * @param <T> The type of event to handle.
 */
public abstract class AbstractHandler<T> implements Saga, NeedContext, DescribesHandlers {
    private final Class<T> handlerType;
    private ExecutionContext context;

    /**
     * Construct instance using the actual handler type.
     *
     * <p>This is necessary as it is slow (and error prone) to detect the generic argument
     * type at runtime.</p>
     */
    protected AbstractHandler(final Class<T> handlerType) {
        this.handlerType = handlerType;
    }

    /**
     * Override this method in your custom handler class to
     * handle saga-lib events of type {@code T}.
     */
    public abstract void handle(T event);

    @Override
    public HandlerDescription describeHandlers() {
        return HandlerDescriptions.
                startedBy(handlerType).usingMethod(this::handle)
                .finishDescription();
    }

    /**
     * Always returns the same invalid state instance.
     */
    @Override
    public final SagaState state() {
        return InvalidState.INSTANCE;
    }

    /**
     * Returns the current message execution context.
     */
    protected ExecutionContext context() {
        return context;
    }

    /**
     * Ignored. No state is saved.
     */
    @Override
    public final void setState(final SagaState state) {
    }

    /**
     * Ignored not necessary. State instance is always invalid.
     */
    @Override
    public final void createNewState() {
    }

    /**
     * Always returns true, resulting in no state to be saved ever.
     */
    @Override
    public final boolean isFinished() {
        return true;
    }

    /**
     * Return empty list. Does not need to map between different messages.
     */
    @Override
    public final Collection<KeyReader> keyReaders() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {
        this.context = executionContext;
    }
}
