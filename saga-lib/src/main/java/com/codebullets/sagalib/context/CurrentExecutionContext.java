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
package com.codebullets.sagalib.context;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;

/**
 * Allows editing of current execution context data.
 */
public interface CurrentExecutionContext extends ExecutionContext {
    /**
     * Sets the current message being handled.
     */
    void setMessage(Object message);

    /**
     * Sets the current saga handler instance being executed.
     */
    void setSaga(Saga saga);

    /**
     * Sets the parent context of the execution.
     */
    void setParentContext(ExecutionContext parentContext);

    /**
     * Sets optional error information, that occurred during handling.
     */
    void setError(Exception error);

    /**
     * Records the identifier of the saga stored. This includes the current
     * and parent context.
     */
    void recordSagaStateStored(String sagaId);

    /**
     * Checks whether a saga state was stored as part of full execution chain.
     * This includes sagas handles as part of child and parent contexts.
     *
     * <p>This is used to determine whether a saga state needs
     * deletion in case the saga is finished, but has already been saved in
     * possible recursive executions.</p>
     */
    boolean hasBeenStored(String sagaId);
}