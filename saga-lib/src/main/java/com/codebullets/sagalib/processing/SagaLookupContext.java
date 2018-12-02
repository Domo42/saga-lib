/*
 * Copyright 2015 Stefan Domnanovits
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

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.HeaderName;
import com.codebullets.sagalib.Headers;
import com.codebullets.sagalib.context.LookupContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds the context information provided during saga lookup.
 */
class SagaLookupContext implements LookupContext {
    private final ExecutionContext parentContext;

    private Object message;
    private Map<HeaderName<?>, Object> headers;

    /**
     * Generates a new instance of SagaLookupContext.
     */
    SagaLookupContext(final Object message, @Nullable final ExecutionContext parentContext) {
        this.message = message;
        this.headers = new HashMap<>();
        this.parentContext = parentContext;
    }

    /**
     * Generates a new instance of SagaLookupContext.
     */
    SagaLookupContext(final Object message, final Map<HeaderName<?>, Object> headers, @Nullable final ExecutionContext parentContext) {
        this.message = message;
        this.parentContext = parentContext;
        this.headers = new HashMap<>(headers);
    }

    /**
     * Generates a new instance of SagaLookupContext.
     */
    SagaLookupContext(final Object message, final LookupContext baseContext, @Nullable final ExecutionContext parentContext) {
        this (message, parentContext);
        headers = Headers.copyFromStream(baseContext.getAllHeaders());
    }

    @Override
    public Object message() {
        return message;
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

    /**
     * Creates a new saga lookup context for a message.
     * @return Returns a new lookup context instance.
     */
    public static LookupContext forMessage(final Object message) {
        return new SagaLookupContext(message, null);
    }
}