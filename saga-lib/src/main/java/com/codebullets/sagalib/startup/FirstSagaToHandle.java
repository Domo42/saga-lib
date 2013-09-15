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

import com.codebullets.sagalib.Saga;

import java.util.List;

/**
 * Selects the first saga type to be executed in case multiple sagas match
 * a specific message type.
 */
public class FirstSagaToHandle {
    private final List<Class<? extends Saga>> orderedTypes;
    private final StreamBuilder builder;

    /**
     * Generates a new instance of FirstSagaToHandle.
     */
    public FirstSagaToHandle(final List<Class<? extends Saga>> orderedTypes, final StreamBuilder builder) {
        this.orderedTypes = orderedTypes;
        this.builder = builder;
    }

    /**
     * Define the first saga type to execute in case a message matches multiple ones.
     */
    public NextSagaToHandle firstExecute(final Class<? extends Saga> first) {
        orderedTypes.add(0, first);
        return new NextSagaToHandle(orderedTypes, builder);
    }
}