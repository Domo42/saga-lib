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
package com.codebullets.sagalib;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Add messages and events to the steam so that they are processed and
 * trigger saga events.
 * <p>There are two ways saga execution is triggered</p>
 * <ul>
 *     <li>calling one of the {@code add()} methods: This will execute
 *     the sagas asynchronously based on the executor provided during the
 *     saga-lib instantiation.</li>
 *     <li>calling one of the {@code handle()} methods: This will execute
 *     the sagas right away on the current call stack.</li>
 * </ul>
 */
public interface MessageStream {
    /**
     * Add a new message to be processed by the saga lib. The message can be of any type.
     * Message is handled in the background and not necessarily a synchronous operation
     * depending on the execution strategy.
     * @param message The message to be handled.
     */
    void add(@Nonnull Object message);

    /**
     * Add a new message to be processed by the saga lib. The message can be of any type.
     * Message is handled in the background and not necessarily a synchronous operation
     * depending on the execution strategy.
     * @param message The message to be handled.
     * @param headers A list of header values not part of the messages. These value
     *                can be accessed within the sagas from the {@code ExecutionContext}.
     */
    void add(@Nonnull Object message, @Nullable Map<String, Object> headers);

    /**
     * Handles the given message on synchronously on the the calling thread.
     * @param message The message to be handled.
     *
     * @throws InvocationTargetException Thrown when invocation of handler method on saga fails.
     * @throws IllegalAccessException Thrown when access to the method to invoke is denied.
     */
    void handle(@Nonnull Object message) throws InvocationTargetException, IllegalAccessException;

    /**
     * Handles the given message on synchronously on the the calling thread. The parent context
     * might provides information for handles when saga are executed in a nested fashion.
     *
     * <p>Header values are copied over from the parent context on to the new execution context.</p>
     *
     * @param message The message to be handled.
     * @param parentContext The parent context from which the new saga handling is triggered.
     *
     * @throws InvocationTargetException Thrown when invocation of handler method on saga fails.
     * @throws IllegalAccessException    Thrown when access to the method to invoke is denied.
     */
    void handle(@Nonnull Object message, @Nullable ExecutionContext parentContext) throws InvocationTargetException, IllegalAccessException;

    /**
     * Handles the given message on synchronously on the the calling thread.
     * @param message The message to be handled.
     * @param headers A list of header values not part of the messages. These value
     *                can be accessed within the sagas from the {@code ExecutionContext}.
     *
     * @throws InvocationTargetException Thrown when invocation of handler method on saga fails.
     * @throws IllegalAccessException    Thrown when access to the method to invoke is denied.
     */
    void handle(@Nonnull Object message, @Nullable Map<String, Object> headers) throws InvocationTargetException, IllegalAccessException;

    /**
     * Handles the given message on synchronously on the the calling thread. The parent context
     * might provides information for handles when saga are executed in a nested fashion.
     *
     * <p>Header values are copied over from the parent context on to the new execution context.
     * Header values provided with the {@code headers} parameter will added to the
     * the currents context header as well and override possible existing values.</p>
     *
     * @param message The message to be handled.
     * @param headers A list of header values not part of the messages. These value
     *                can be accessed within the sagas from the {@code ExecutionContext}.
     * @param parentContext The parent context from which the new saga handling is triggered.
     *
     * @throws InvocationTargetException Thrown when invocation of handler method on saga fails.
     * @throws IllegalAccessException    Thrown when access to the method to invoke is denied.
     */
    void handle(@Nonnull Object message, @Nullable Map<String, Object> headers, @Nullable ExecutionContext parentContext)
            throws InvocationTargetException, IllegalAccessException;
}
