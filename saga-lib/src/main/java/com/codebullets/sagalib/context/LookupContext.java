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
package com.codebullets.sagalib.context;

import com.codebullets.sagalib.ExecutionContext;

import javax.annotation.Nullable;

/**
 * Provides context information when looking for and building sagas
 * to use for message handling.
 */
public interface LookupContext {
    /**
     * Gets the current message handled as part of the current handler chain.
     */
    Object message();

    /**
     * Gets the list of available header values.
     */
    Iterable<String> getHeaders();

    /**
     * Gets the value of a specific header. If the header is not defined <em>null</em> is returned.
     */
    Object getHeaderValue(String header);

    /**
     * Sets a specific header value.
     */
    void setHeaderValue(String header, Object value);

    /**
     * Gets the optional parent execution context. A parent context might exist if
     * saga handling was triggered within another saga.
     *
     * @return Returns the current parent context or {@code null} if no parent
     * context is available.
     */
    @Nullable
    ExecutionContext parentContext();
}