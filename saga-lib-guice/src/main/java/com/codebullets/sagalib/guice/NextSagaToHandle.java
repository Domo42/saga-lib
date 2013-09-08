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
package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.Saga;

import java.util.Collection;

/**
 * Defines the saga handlers following the first one.
 */
public class NextSagaToHandle {
    private final Collection<Class<? extends Saga>> orderedTypes;
    private final SagaModuleBuilder builder;

    /**
     * Generates a new instance of NextSagaToHandle.
     */
    public NextSagaToHandle(final Collection<Class<? extends Saga>> orderedTypes, final SagaModuleBuilder builder) {
        this.orderedTypes = orderedTypes;
        this.builder = builder;
    }

    /**
     * Define the next saga type to handle a message to define handler ordering.
     */
    public NextSagaToHandle then(final Class<? extends Saga> next) {
        orderedTypes.add(next);
        return this;
    }

    /**
     * Returns the originating builder.
     */
    public SagaModuleBuilder builder() {
        return builder;
    }
}