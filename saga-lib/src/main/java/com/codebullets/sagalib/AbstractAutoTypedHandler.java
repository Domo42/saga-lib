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
import com.google.common.reflect.TypeToken;

import java.util.Collection;
import java.util.Collections;

/**
 * Helper class to implement a saga handling a single message that is automatically
 * finished. No state will be saved.
 *
 * <p>This class is similar to {@link AbstractHandler} but tries to infer the actual
 * generic type via reflection. As such this handler is slower in comparison. On
 * average it took one additional micro second to determine the type. This halved
 * the number of possible ops/s on my test machine.</p>
 *
 * @param <T> The type of event being handled.
 */
public abstract class AbstractAutoTypedHandler<T> implements Saga, DescribesHandlers, NeedContext {
    private final TypeToken<T> typeToken = new TypeToken<T>(getClass()) { };
    private ExecutionContext context;

    /**
     * Override this method in your custom handler class to
     * handle saga-lib events of type {@code T}.
     */
    public abstract void handle(final T event);

    @Override
    public HandlerDescription describeHandlers() {
        return HandlerDescriptions.
                startedBy(typeToken.getRawType()).usingMethod((e) -> handle((T) e))
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
