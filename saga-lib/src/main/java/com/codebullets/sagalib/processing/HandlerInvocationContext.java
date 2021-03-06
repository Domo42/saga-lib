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

package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.invocation.InvocationContext;
import com.codebullets.sagalib.processing.invocation.InvocationHandlerType;

/**
 * Holds the context information as the saga handler is to be
 * invoked on the target saga instance.
 */
class HandlerInvocationContext implements InvocationContext {
    private final ExecutionContext context;
    private InvocationHandlerType handlerType;

    HandlerInvocationContext(final ExecutionContext context) {
        this.context = context;
    }

    @Override
    public ExecutionContext context() {
        return context;
    }

    void setHandlerType(final InvocationHandlerType type) {
        this.handlerType = type;
    }

    @Override
    public InvocationHandlerType handlerType() {
        return handlerType;
    }

    @Override
    public Saga<?> saga() {
        return context.saga();
    }

    @Override
    public Object message() {
        return context.message();
    }
}
