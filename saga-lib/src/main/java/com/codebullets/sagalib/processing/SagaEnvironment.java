/*
 * Copyright 2014 Stefan Domnanovits
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

import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;

import javax.inject.Provider;

/**
 * Collects saga environment instances used during saga execution.
 */
final class SagaEnvironment {
    private final TimeoutManager timeoutManager;
    private final StateStorage storage;
    private final SagaFactory sagaFactory;
    private final Provider<CurrentExecutionContext> contextProvider;

    /**
     * Generates a new instance of SagaEnvironment.
     */
    private SagaEnvironment(
            final TimeoutManager timeoutManager,
            final StateStorage storage,
            final SagaFactory sagaFactory,
            final Provider<CurrentExecutionContext> contextProvider) {
        this.timeoutManager = timeoutManager;
        this.storage = storage;
        this.sagaFactory = sagaFactory;
        this.contextProvider = contextProvider;
    }

    /**
     * Gets the timeout manager.
     */
    public TimeoutManager timeoutManager() {
        return timeoutManager;
    }

    /**
     * Gets the state storage.
     */
    public StateStorage storage() {
        return storage;
    }

    /**
     * Gets the saga factory.
     */
    public SagaFactory sagaFactory() {
        return sagaFactory;
    }

    /**
     * Gets the execution context provider.
     */
    public Provider<CurrentExecutionContext> contextProvider() {
        return contextProvider;
    }

    public static SagaEnvironment create(
            final TimeoutManager timeoutManager,
            final StateStorage storage,
            final SagaFactory sagaFactory,
            final Provider<CurrentExecutionContext> contextProvider) {
        return new SagaEnvironment(timeoutManager, storage, sagaFactory, contextProvider);
    }
}