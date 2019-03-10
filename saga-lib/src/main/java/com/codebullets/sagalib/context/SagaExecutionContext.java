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
import com.codebullets.sagalib.HeaderName;
import com.codebullets.sagalib.Saga;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Execution context used in the saga lib.
 */
public class SagaExecutionContext implements CurrentExecutionContext {
    private boolean dispatchingStopped;
    private Object message;
    private Saga saga;
    private Map<HeaderName<?>, Object> headers = new HashMap<>();
    private Set<String> storedSagas = new HashSet<>();

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
        // Iterable contract requires the ability to
        // scan over the items multiple times, which is something
        // Stream does not support
        return headers.keySet().stream()
                .map(HeaderName::toString)
                .collect(Collectors.toList());
    }

    @Override
    public Stream<Map.Entry<HeaderName<?>, Object>> getAllHeaders() {
        return headers.entrySet().stream();
    }

    @Override
    public Object getHeaderValue(final String header) {
        return headers.get(HeaderName.forName(header));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> getHeaderValue(final HeaderName<T> header) {
        return Optional.ofNullable((T) headers.get(header));
    }

    @Override
    public void setHeaderValue(final String header, final Object value) {
        headers.put(HeaderName.forName(header), value);
    }

    @Override
    public <T> void setHeaderValue(final HeaderName<T> header, final T value) {
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

    @Override
    public void recordSagaStateStored(final String sagaId) {
        // always store in parent context if available.
        if (parentContext instanceof CurrentExecutionContext) {
            ((CurrentExecutionContext) parentContext).recordSagaStateStored(sagaId);
        } else {
            storedSagas.add(sagaId);
        }
    }

    @Override
    public boolean hasBeenStored(final String sagaId) {
        boolean hasBeenStored;

        if (parentContext instanceof CurrentExecutionContext) {
            hasBeenStored = ((CurrentExecutionContext) parentContext).hasBeenStored(sagaId);
        } else {
            hasBeenStored = storedSagas.contains(sagaId);
        }

        return hasBeenStored;
    }
}