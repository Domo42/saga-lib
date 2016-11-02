/*
 * Copyright 2015 Stefan Domnanovits
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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.timeout.NeedTimeouts;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.concurrent.ExecutionException;

/**
 * Caches a list of providers based on the saga type to be created. Creates
 * a new saga instance based on provider instance of the type.
 */
public class SagaInstanceCreator {
    private final LoadingCache<Class<? extends Saga>, Provider<? extends Saga>> providers;
    private final TimeoutManager timeoutManager;

    /**
     * Generates a new instance of ProviderCache.
     */
    @Inject
    public SagaInstanceCreator(final SagaProviderFactory providerFactory, final TimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
        // Create providers when needed. Cache providers for later use.
        providers = CacheBuilder.newBuilder().build(new ProviderLoader(providerFactory));
    }

    /**
     * Creates a new saga instances with the requested type.
     * @throws ExecutionException Is thrown in case no provider can be found to create an instance.
     *                            The actual cause can be inspected by {@link ExecutionException#getCause()}.
     */
    public Saga createNew(final Class<? extends Saga> sagaType) throws ExecutionException {
        Saga newInstance = createNewInstance(sagaType);
        if (newInstance instanceof NeedTimeouts) {
            ((NeedTimeouts) newInstance).setTimeoutManager(timeoutManager);
        }

        return newInstance;
    }

    private Saga createNewInstance(final Class<? extends Saga> sagaType) throws ExecutionException {
        Provider<? extends Saga> sagaProvider = providers.get(sagaType);
        return sagaProvider.get();
    }

    /**
     * Creates a new provider on demand.
     */
    private final static class ProviderLoader extends CacheLoader<Class<? extends Saga>, Provider<? extends Saga>> {
        private final SagaProviderFactory providerFactory;

        private ProviderLoader(final SagaProviderFactory providerFactory) {
            this.providerFactory = providerFactory;
        }

        @Override
        public Provider<? extends Saga> load(final Class<? extends Saga> key) {
            Provider<? extends Saga> provider = providerFactory.createProvider(key);
            if (provider == null) {
                throw new IllegalStateException("There is no provider to create sagas of type " + key.getName());
            }

            return provider;
        }
    }
}