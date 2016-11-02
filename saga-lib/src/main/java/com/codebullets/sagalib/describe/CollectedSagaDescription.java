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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This description is the finalized concrete result of a call to
 * {@link DirectDescription#describe()}.
 */
@Immutable
class CollectedSagaDescription implements SagaDescription {
    private final Class<?> startedBy;
    private final Collection<HandlerDefinition> handlers;

    CollectedSagaDescription(final Class<?> startedBy, final Collection<HandlerDefinition> handlers) {
        this.startedBy = startedBy;
        this.handlers = handlers;
    }

    @Override
    public Class<?> startedBy() {
        return startedBy;
    }

    @Override
    public Iterable<Class<?>> handlerTypes() {
        Stream<Class<?>> types = handlers.stream().map(HandlerDefinition::handlerType);
        return types::iterator;
    }

    @Override
    public Consumer<Object> handler() {
        return this::invokeTargetHandler;
    }

    @SuppressWarnings("unchecked")
    private void invokeTargetHandler(final Object message) {
        final Optional<HandlerDefinition> targetHandler = handlers.stream()
                .filter(h -> h.handlerType().isAssignableFrom(message.getClass()))
                .findFirst();

        targetHandler.map(h -> (Consumer<Object>) h.handlerMethod())
                .ifPresent(m -> m.accept(message));
    }
}
