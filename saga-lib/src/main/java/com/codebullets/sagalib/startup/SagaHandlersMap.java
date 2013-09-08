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
package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.Saga;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Holds the list of handler based on saga type.
 */
public class SagaHandlersMap {
    private final Class<? extends Saga> sagaType;
    private final Collection<MessageHandler> supportedHandlers = new HashSet<>();

    /**
     * Generates a new instance of SagaHandlersMap.
     */
    public SagaHandlersMap(final Class<? extends Saga> sagaType) {
        this.sagaType = sagaType;
    }

    /**
     * Gets the type of saga handling the message events.
     */
    public Class<? extends Saga> getSagaType() {
        return sagaType;
    }

    /**
     * Add a single message type to be supported by a saga type. Appends to the list
     * of supported types of a saga.
     */
    public void add(final MessageHandler supportedHandler) {
        supportedHandlers.add(supportedHandler);
    }

    /**
     * Returns a collection of message types handled by a saga.
     */
    public Collection<MessageHandler> messageHandlers() {
        return Collections.unmodifiableCollection(supportedHandlers);
    }
}
