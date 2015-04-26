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

import com.codebullets.sagalib.context.LookupContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the context information provided during saga lookup.
 */
class SagaLookupContext implements LookupContext {
    private Object message;
    private Map<String, Object> headers = new HashMap<>();

    /**
     * Generates a new instance of SagaLookupContext.
     */
    public SagaLookupContext(final Object message) {
        this.message = message;
        this.headers = new HashMap<>();
    }

    /**
     * Generates a new instance of SagaLookupContext.
     */
    public SagaLookupContext(final Object message, final Map<String, Object> headers) {
        this.message = message;
        this.headers = new HashMap<>(headers);
    }

    /**
     * Generates a new instance of SagaLookupContext.
     */
    public SagaLookupContext(final Object message, final LookupContext baseContext) {
        this (message);
        for (String key : baseContext.getHeaders()) {
            headers.put(key, baseContext.getHeaderValue(key));
        }
    }

    @Override
    public Object message() {
        return message;
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

    /**
     * Creates a new saga lookup context for a message.
     * @return Returns a new lookup context instance.
     */
    public static LookupContext forMessage(final Object message) {
        return new SagaLookupContext(message);
    }
}