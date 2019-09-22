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

package com.codebullets.sagalib.order;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;

import javax.inject.Provider;

class InstanceCreators implements SagaProviderFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
        Provider<?> sagaProvider = null;

        if (sagaClass.equals(SagaWithState.class)) {
            sagaProvider = SagaWithState::new;
        } else if (sagaClass.equals(StartingHandler.class)) {
            sagaProvider = StartingHandler::new;
        }  else if (sagaClass.equals(StartingHandler2.class)) {
            sagaProvider = StartingHandler2::new;
        }

        return (Provider<T>) sagaProvider;
    }
}
