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

/**
 * Information about method handling a message on a saga.
 */
public class MessageHandler {
    private final boolean startsSaga;
    private final Class<?> messageType;
    private final Class<? extends Saga> sagaType;

    /**
     * Generates a new instance of MessageHandler.
     */
    public MessageHandler(Class<?> messageType, Class<? extends Saga> sagaType) {
        this(messageType, sagaType, false);
    }

    /**
     * Generates a new instance of MessageHandler.
     */
    public MessageHandler(Class<?> messageType, Class<? extends Saga> sagaType, boolean startsSaga) {
        this.startsSaga = startsSaga;
        this.sagaType = sagaType;
        this.messageType = messageType;
    }

    /**
     * Gets a value indicating whether the message handler is supposed to
     * start a sage.
     */
    public boolean getStartsSaga() {
        return startsSaga;
    }

    /**
     * Gets the type of message that is handled.
     */
    public Class<?> getMessageType() {
        return messageType;
    }

    /**
     * Gets the type of the saga the handler is attached to.
     */
    public Class<? extends Saga> getSagaType() {
        return sagaType;
    }
}
