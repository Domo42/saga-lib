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

import java.util.function.Consumer;

/**
 * Description containing all information about the messages handled
 * as well as a way of handling them.
 */
public interface SagaDescription {
    /**
     * Gets the type of the message from which the saga is started.
     */
    Class<?> startedBy();

    /**
     * Gets a list of message types handled by this saga.
     */
    Iterable<Class<?>> handlerTypes();

    /**
     * Returns a consumer taking the message, calling the specific saga handler
     * method.
     */
    Consumer<Object> handler();
}
