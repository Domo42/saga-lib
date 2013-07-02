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

import com.codebullets.sagalib.timeout.NeedTimeouts;
import com.codebullets.sagalib.timeout.TimeoutManager;

import java.util.concurrent.TimeUnit;

/**
 * Base implementation of the {@link Saga} interface.
 *
 * @param <SAGA_STATE> Type of the state object attached to this saga.
 */
public abstract class AbstractSaga<SAGA_STATE extends SagaState> implements Saga<SAGA_STATE>, NeedTimeouts {
    private SAGA_STATE state;
    private boolean completed;
    private TimeoutManager timeoutManager;

    /**
     * Generates a new instance of AbstractSaga.
     */
    protected AbstractSaga() {
        completed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SAGA_STATE state() {
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(final SAGA_STATE state) {
        this.state = state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks this saga as completed. Resulting in the deletion of all saga state
     * associated.
     */
    protected void setCompleted() {
        completed = true;
    }

    /**
     * Requests a timeout event to be sent back to this saga.
     */
    protected void requestTimeout(final long delay, final TimeUnit unit) {
        requestTimeout(delay, unit, null);
    }

    /**
     * Requests a timeout event with a specific name to this saga. The name can
     * be used to distinguish the timeout if multiple ones have been requested by the saga.
     */
    protected  void requestTimeout(final long delay, final TimeUnit unit, final String name) {
        timeoutManager.requestTimeout(state().getSagaId(), name, delay, unit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeoutManager(final TimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }
}
