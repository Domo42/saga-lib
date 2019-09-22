/*
 * Copyright 2018 Stefan Domnanovits
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.codebullets.sagalib;

import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.timeout.TimeoutManager;

import javax.inject.Provider;
import java.util.Set;

class TestSagaProviderFactory implements SagaProviderFactory {
    private final TimeoutManager timeoutManager;
    private final Set<Number> numbers;
    private final Set<String> calledSagas;

    /**
     * Generates a new instance of MessageStreamTest$TestSagaProviderFactory.
     */
     TestSagaProviderFactory(TimeoutManager timeoutManager, Set<Number> numbers, final Set<String> calledSagas) {
        this.timeoutManager = timeoutManager;
        this.numbers = numbers;
        this.calledSagas = calledSagas;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
        Provider provider = null;

        if (sagaClass.equals(TestSaga.class)) {
            provider = () -> new TestSaga(timeoutManager);
        } else if (sagaClass.equals(NumberSaga.class)) {
            provider = () -> new NumberSaga(numbers);
        } else if (sagaClass.equals(IntegerSaga.class)) {
            provider = () -> new IntegerSaga(calledSagas);
        } else if (sagaClass.equals(DeadMessageSaga.class)) {
            provider = () -> new DeadMessageSaga();
        }

        return (Provider<T>) provider;
    }
}
