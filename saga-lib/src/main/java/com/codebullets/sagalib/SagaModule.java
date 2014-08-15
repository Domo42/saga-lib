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
package com.codebullets.sagalib;

/**
 * A module is called every time before and after a message is handled. In can be used
 * to prepare and cleanup state used during executions of a saga.
 * <p>A module should be used to perform message independent code. If there is logic
 * that needs to be executed based on the message type it is better to write a specific
 * saga for it.</p>
 */
public interface SagaModule {
    /**
     * Is called before any saga handler is executed.
     */
    void onStart(final ExecutionContext context);

    /**
     * Is called after all saga handlers have been executed.
     */
    void onFinished(final ExecutionContext context);

    /**
     * This method is called in case there has been an exception during
     * the execution of saga handlers. It is executed before {@link #onFinished(ExecutionContext)}.
     * <p>If there is no error this method will not be called.</p>
     */
    void onError(final ExecutionContext context, Object message, Throwable error);
}