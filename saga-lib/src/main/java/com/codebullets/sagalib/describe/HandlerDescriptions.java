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

/**
 * This is the starting point to create a new saga handling description.
 */
public final class HandlerDescriptions {
    private HandlerDescriptions() { }

    /**
     * Starts a new description, be defining the initial message to be handled.
     *
     * <p>The complete description is created by chaining a definition of handled
     * messages and methods together.</p>
     *
     * <pre>
     * {@literal @}Override
     * public HandlerDescription describeHandlers() {
     *     return HandlerDescriptions
     *         .startedBy(StartingMessage.class).usingMethod(this::startingSagaMethod)
     *         .handleMessage(OtherMessage.class).usingMethod(this::otherSagaMethod)
     *         .finishDescription();
     * }
     * </pre>
     *
     * @param <T> The type of the starting saga message.
     */
    public static <T> HandlerMethodDefinition<T> startedBy(final Class<T> startingType) {
        return new DescriptionCollector<>(startingType);
    }
}
