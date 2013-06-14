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

/**
 * Represents a single saga. A saga contains state and handles different messages and events.
 */
public interface Saga<SAGA_STATE extends SagaState> {
    /**
     * Gets the current state of the saga.
     */
    SAGA_STATE state();

    /**
     * Sets the current state of the sage. When {@link #createNewState()} is called the implementer
     * should use this method to set the specific saga instance state.<br/>
     * When loading an already running saga this method is called by the saga-lib before
     * any of the handler methods are called.<p/>
     * Once set the {@link #state()} method should return the provided instance.
     */
    void setState(SAGA_STATE state);

    /**
     * Instructs the sage to create a new empty instance of the saga state. After
     * this method has been called {@link #state()} is expected to return the new instance.
     */
    void createNewState();

    /**
     * Indicates whether the saga has completed.
     * @return True if saga is complete; otherwise false.
     */
    boolean isCompleted();
}
