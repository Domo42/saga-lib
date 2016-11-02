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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Collects all description data provided.
 * @param <T> The starting message type of the saga described.
 */
class DescriptionCollector<T> implements HandlerTypeDefinition, HandlerMethodDefinition<T> {
    private final Class<T> startingType;
    private final Collection<CollectingHandlerDefinition<?>> handlerDefinitions = new ArrayList<>();

    DescriptionCollector(final Class<T> startingType) {
        this.startingType = startingType;
    }

    @Override
    public <NEW_MESSAGE> HandlerMethodDefinition<NEW_MESSAGE> handleMessage(final Class<NEW_MESSAGE> messageType) {
        requireNonNull(messageType, "Starting message type must not be null.");
        return new CollectingHandlerDefinition<>(this, messageType);
    }

    @Override
    public SagaDescription finishDescription() {
        List<HandlerDefinition> handlers = handlerDefinitions.stream()
                .map(d -> new HandlerDefinition(d.messageType, d.handler))
                .collect(Collectors.toList());

        return new CollectedSagaDescription(startingType, handlers);
    }

    @Override
    public HandlerTypeDefinition usingMethod(final Consumer<T> handlerMethod) {
        requireNonNull(handlerMethod, "Handler method for type " + startingType + " must not be null.");
        handlerDefinitions.add(new CollectingHandlerDefinition<T>(this, startingType, handlerMethod));

        return this;
    }

    /**
     * Collects all the handling definition data over time. This is done
     * by calling {@code usingMethod}.
     * @param <T> The type of message handled.
     */
    private final static class CollectingHandlerDefinition<T> implements HandlerMethodDefinition<T> {
        private final DescriptionCollector descriptionCollector;
        private final Class<T> messageType;
        private Consumer<T> handler;

        private CollectingHandlerDefinition(
                final DescriptionCollector descriptionCollector,
                final Class<T> messageType) {
            this.descriptionCollector = descriptionCollector;
            this.messageType = messageType;
        }

        private CollectingHandlerDefinition(
                final DescriptionCollector descriptionCollector,
                final Class<T> messageType,
                final Consumer<T> handlerMethod) {
            this.descriptionCollector = descriptionCollector;
            this.messageType = messageType;
            this.handler = handlerMethod;
        }

        @Override
        public HandlerTypeDefinition usingMethod(final Consumer<T> handlerMethod) {
            this.handler = requireNonNull(handlerMethod, "Handler method for type " + messageType + " must not be null.");
            this.descriptionCollector.handlerDefinitions.add(this);
            return descriptionCollector;
        }
    }
}
