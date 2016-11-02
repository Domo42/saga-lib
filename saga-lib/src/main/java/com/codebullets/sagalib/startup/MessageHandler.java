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

import com.codebullets.sagalib.describe.DirectDescription;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Information about method handling a message on a saga.
 */
public class MessageHandler {
    private final boolean startsSaga;
    private final Class<?> messageType;

    @Nullable
    private final Method methodToInvoke;

    /**
     * Generates a new instance of MessageHandler.
     */
    public MessageHandler(final Class<?> messageType, @Nullable final Method methodToInvoke, final boolean startsSaga) {
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
     * Gets the optional method to invoke to trigger the message handler.
     * If the method is empty, it indicates that the saga implements {@link DirectDescription}
     * and one can use the returned consumer to execute message handling.
     */
    public Optional<Method> getMethodToInvoke() {
        return Optional.ofNullable(methodToInvoke);
    }

    /**
     * Creates new handler, with the reflection information about the method to call.
     */
    public static MessageHandler reflectionInvokedHandler(final Class<?> messageType, final Method methodToInvoke, final boolean startsSaga) {
        return new MessageHandler(messageType, methodToInvoke, startsSaga);
    }

    /**
     * Creates a new handler definition of the message, indicating that the parent saga
     * provides its own handler through via a direct description.
     */
    public static MessageHandler selfDescribedHandler(final Class<?> messageType, final boolean startsSaga) {
        return new MessageHandler(messageType, null, startsSaga);
    }
}
