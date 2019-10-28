/*
 * Copyright 2018 Stefan Domnanovits
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

package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;

/**
 * Provides additional information about the handler to be invoked.
 */
public interface InvocationContext {
    /**
     * Gets the current saga execution context. This context includes
     * additional information like headers values.
     */
    ExecutionContext context();

    /**
     * Gets a value indicating whether the current invocation starts a new
     * saga or continues an existing one.
     */
    InvocationHandlerType handlerType();

    /**
     * Gets the saga instance where the handler invocation is executed.
     */
    Saga<?> saga();

    /**
     * Gets the message of the current saga handling execution.
     */
    Object message();
}
