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
import java.util.Collections;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Collects all description data provided.
 *
 * <p>This class looks quite a bit rough. That's because some tweaking went into it
 * to improve performance. Have a look at 'measurements.txt' of jmh-tests for some
 * of steps and measurements taken.
 * m</p>
 *
 * @param <T> The starting message type of the saga described.
 */
class DescriptionCollector<T> implements HandlerTypeDefinition, HandlerMethodDefinition<T> {
    private final Class<? super T> startingType;
    private Collection<Class<?>> handlerTypes;
    private Consumer<Object> collectedHandler;

    DescriptionCollector(final Class<? super T> startingType) {
        this.startingType = startingType;
    }

    @Override
    public <NEW_MESSAGE> HandlerMethodDefinition<NEW_MESSAGE> handleMessage(final Class<NEW_MESSAGE> messageType) {
        requireNonNull(messageType, "Starting message type must not be null.");
        return new CollectingHandlerDefinition<>(this, messageType);
    }

    @Override
    public HandlerDescription finishDescription() {
        CollectedSagaDescription description;

        if (handlerTypes != null) {
            description = new CollectedSagaDescription(startingType, handlerTypes, collectedHandler);
        } else {
            description = new CollectedSagaDescription(startingType, Collections.singleton(startingType), collectedHandler);
        }

        return description;
    }

    @Override
    public HandlerTypeDefinition usingMethod(final Consumer<T> handlerMethod) {
        requireNonNull(handlerMethod, "Handler method for type " + startingType + " must not be null.");

        collectedHandler = (Consumer<Object>) handlerMethod;

        return this;
    }

    private void addHandlerType(final Class<?> handlerType) {
        if (handlerTypes == null) {
            handlerTypes = new ArrayList<>();
            handlerTypes.add(startingType);
        }

        handlerTypes.add(handlerType);
    }

    /**
     * Collects all the handling definition data over time. This is done
     * by calling {@code usingMethod}.
     * @param <MSG> The type of message handled.
     */
    private static final class CollectingHandlerDefinition<MSG> implements HandlerMethodDefinition<MSG> {
        private final Class<?> messageType;
        private DescriptionCollector collector;

        private CollectingHandlerDefinition(
                final DescriptionCollector collector,
                final Class<MSG> messageType) {
            this.collector = collector;
            this.messageType = messageType;
        }

        @Override
        public HandlerTypeDefinition usingMethod(final Consumer<MSG> handlerMethod) {
            collector.addHandlerType(messageType);

            Consumer<Object> originalHandler = collector.collectedHandler;
            collector.collectedHandler = (msg) -> {
                if (msg.getClass().isAssignableFrom(messageType)) {
                    ((Consumer<Object>) handlerMethod).accept(msg);
                } else {
                    originalHandler.accept(msg);
                }
            };

            return collector;
        }
    }
}
