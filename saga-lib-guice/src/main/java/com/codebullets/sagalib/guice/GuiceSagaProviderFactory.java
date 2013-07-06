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
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.google.inject.Injector;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Creates saga provider based on Guice dependency injection container.<p/>
 * Guice has a feature called Just-in-time Bindings <strong>Just-in-time Bindings</strong>.
 * In this context in means as long as a saga is a concrete type Guice will automatically
 * try to create an instance (or a provider) as long as there is a default constructor
 * or a constructor annotated with {@link Inject} to bring in external dependencies.
 */
public class GuiceSagaProviderFactory implements SagaProviderFactory {
    private final Injector guiceInjector;

    /**
     * Generates a new instance of GuiceSagaProviderFactory.
     */
    @Inject
    public GuiceSagaProviderFactory(final Injector injector) {
        guiceInjector = injector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Provider<? extends Saga> createProvider(final Class sagaClass) {
        return guiceInjector.getProvider(sagaClass);
    }
}