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
 * Implementations of this interface are called during certain life-changing
 * events of a saga.
 */
public interface SagaLifetimeInterceptor {
    /**
     * This method is called whenever a new saga is started, right before the
     * execution of the {@code StartsSaga} handler. This is the time
     * a new saga state has been created and is potentially stored for later use.
     */
    void onStarting(Saga<?> saga, ExecutionContext context, Object message);

    /**
     * This method is called right before any handler of a saga is called. This
     * includes the start handler as well as all other handlers called as long
     * as the saga is not finished.
     */
    void onHandlerExecuting(Saga<?> saga, ExecutionContext context, Object message);

    /**
     * This method is called after any handler of a saga has been called. This
     * includes the start handler as well as all other handlers
     */
    void onHandlerExecuted(Saga<?> saga, ExecutionContext context, Object message);

    /**
     * This method is called whenever a saga has finished. The call is done
     * after a saga handler has executed and the sagas {@code isFinished()}
     * method returns true.
     */
    void onFinished(Saga<?> saga, ExecutionContext context);
}