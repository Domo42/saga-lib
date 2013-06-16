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

import java.lang.reflect.Method;

/**
 * Information about method handling a message on a saga.
 */
public class MessageHandler {
    private final boolean startsSaga;
    private final Class<?> messageType;
    private final Method methodToInvoke;

    /**
     * Generates a new instance of MessageHandler.
     */
    public MessageHandler(final Class<?> messageType, final Method methodToInvoke) {
        this(messageType, methodToInvoke, false);
    }

    /**
     * Generates a new instance of MessageHandler.
     */
    public MessageHandler(final Class<?> messageType, final Method methodToInvoke, final boolean startsSaga) {
        this.startsSaga = startsSaga;
        this.methodToInvoke = methodToInvoke;
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
     * Gets the method to invoke to trigger the message handler.
     */
    public Method getMethodToInvoke() {
        return methodToInvoke;
    }
}
