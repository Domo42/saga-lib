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
import java.util.Collection;
import java.util.function.Consumer;

/**
 * This description is the finalized concrete result of a call to
 * {@link DescribesHandlers#describeHandlers()}.
 */
@Immutable
class CollectedSagaDescription implements HandlerDescription {
    private final Class<?> startedBy;
    private final Collection<Class<?>> handlerTypes;
    private final Consumer<Object> executionHandler;

    CollectedSagaDescription(
            final Class<?> startedBy,
            final Collection<Class<?>> handlerTypes,
            final Consumer<Object> executionHandler) {
        this.startedBy = startedBy;
        this.handlerTypes = handlerTypes;
        this.executionHandler = executionHandler;
    }

    @Override
    public Class<?> startedBy() {
        return startedBy;
    }

    @Override
    public Iterable<Class<?>> handlerTypes() {
        return handlerTypes;
    }

    @Override
    public Consumer<Object> handler() {
        return executionHandler;
    }
}
