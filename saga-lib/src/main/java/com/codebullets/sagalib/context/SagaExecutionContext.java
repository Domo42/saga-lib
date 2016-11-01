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
package com.codebullets.sagalib.context;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Execution context used in the saga lib.
 */
public class SagaExecutionContext implements CurrentExecutionContext {
    private boolean dispatchingStopped;
    private Object message;
    private Saga saga;
    private Map<String, Object> headers = new HashMap<>();

    @Nullable
    private ExecutionContext parentContext;

    @Nullable
    private Exception error;

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopDispatchingCurrentMessageToHandlers() {
        dispatchingStopped = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchingStopped() {
        return dispatchingStopped;
    }

    @Override
    public Object message() {
        return message;
    }

    @Override
    public Saga saga() {
        return saga;
    }

    @Override
    public Iterable<String> getHeaders() {
        return headers.keySet();
    }

    @Override
    public Object getHeaderValue(final String header) {
        return headers.get(header);
    }

    @Override
    public void setHeaderValue(final String header, final Object value) {
        headers.put(header, value);
    }

    @Nullable
    @Override
    public ExecutionContext parentContext() {
        return parentContext;
    }

    @Override
    public void setMessage(final Object message) {
        this.message = message;
    }

    @Override
    public void setSaga(final Saga saga) {
        this.saga = saga;
    }

    @Override
    public void setParentContext(@Nullable final ExecutionContext parentContext) {
        this.parentContext = parentContext;
    }


    @Override
    public Optional<Exception> error() {
        return Optional.ofNullable(error);
    }

    @Override
    public void setError(@Nullable final Exception error) {
        this.error = error;
    }
}