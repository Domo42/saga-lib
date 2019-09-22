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
package com.codebullets.sagalib;

import java.util.Set;

/**
 * Saga listening for messages of type number.
 */
public class NumberSaga extends AbstractSingleEventSaga {
    private final Set<Number> numbers;

    /**
     * Generates a new instance of NumberSaga.
     */
    NumberSaga(Set<Number> numbers) {
        this.numbers = numbers;
    }

    @StartsSaga
    public void numberMessage(Number number) {
        numbers.add(number);

        context().stopDispatchingCurrentMessageToHandlers();
    }
}