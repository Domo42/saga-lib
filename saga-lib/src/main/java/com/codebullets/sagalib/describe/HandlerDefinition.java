/*
 * Copyright 2016 Stefan Domnanovits
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
package com.codebullets.sagalib.describe;

import javax.annotation.concurrent.Immutable;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This is a complete definition of saga handler, including message type
 * and method to call.
 */
@Immutable
public final class HandlerDefinition {
    private final Class<?> handlerType;
    private final Consumer<?> handlerMethod;

    /**
     *
     */
    public HandlerDefinition(final Class<?> handlerType, final Consumer<?> handlerMethod) {
        this.handlerType = checkNotNull(handlerType, "The handler type must not be null.");
        this.handlerMethod = checkNotNull(handlerMethod, "The method handling " + handlerType + "must not be null.");
    }

    /**
     * Gets the type being handled by the handler.
     */
    public Class<?> handlerType() {
        return handlerType;
    }

    /**
     * Gets the method performing the hanlding of  {@link #handlerType()}.
     */
    public Consumer<?> handlerMethod() {
        return handlerMethod;
    }
}
