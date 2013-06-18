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

import java.lang.reflect.InvocationTargetException;

/**
 * Add messages and events to the steam so that they are processed and
 * trigger saga events.
 */
public interface MessageStream {
    /**
     * Add a new message to be processed by the saga lib. The message can be of any type.
     * Message is handled in the background and not necessarily a synchronous operation
     * depending on the execution strategy.
     */
    void add(Object message);

    /**
     * Handles the given message on synchronously on the the calling thread.
     * @throws InvocationTargetException Thrown when invocation of handler method on saga fails.
     * @throws IllegalAccessException Thrown when access to the method to invoke is denied.
     */
    void handle(Object message) throws InvocationTargetException, IllegalAccessException;
}
