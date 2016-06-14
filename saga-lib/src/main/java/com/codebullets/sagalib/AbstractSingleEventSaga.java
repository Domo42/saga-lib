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
package com.codebullets.sagalib;

import com.codebullets.sagalib.context.NeedContext;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * <p>Basic saga implementation handling only one event. No state is
 * saved and no timeout is expected. Only the method annotated with {@link StartsSaga}
 * is called.</p>
 *
 * <p>This class is intended for events where the logic does not need to wait
 * for further events but can finish synchronously. As such no timeout
 * or further event handling is necessary.</p>
 */
public abstract class AbstractSingleEventSaga implements Saga, NeedContext {
    private static final InvalidState INVALID_STATE = new InvalidState();
    private ExecutionContext context;

    /**
     * Always returns null. This saga only handles a single
     */
    @Override
    public SagaState state() {
        return INVALID_STATE;
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
    public void setState(final SagaState state) {
    }

    /**
     * Ignored not necessary. State instance is always invalid.
     */
    @Override
    public void createNewState() {
    }

    /**
     * Always returns true, resulting in no state to be saved ever.
     */
    @Override
    public boolean isFinished() {
        return true;
    }

    /**
     * Return empty list. Does not need to map between different messages.
     */
    @Override
    public Collection<KeyReader> keyReaders() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExecutionContext(final ExecutionContext executionContext) {
        this.context = executionContext;
    }

    /**
     * Always returns the same state values, representing an
     * invalid saga state.
     */
    private static final class InvalidState extends AbstractSagaState {
        private static final Set<String> INSTANCE_KEYS = Collections.unmodifiableSet(Sets.newHashSet("AbstractSingleEventSaga"));

        @Override
        public String getSagaId() {
            return "AbstractSingleEventSaga";
        }

        @Override
        public String getType() {
            return "AbstractSingleEventSaga";
        }

        @Override
        public Set<String> instanceKeys() {
            return INSTANCE_KEYS;
        }
    }
}