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

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.context.ExecutionContext;
import com.codebullets.sagalib.processing.HandlerInvoker;
import com.codebullets.sagalib.processing.KeyExtractor;
import com.codebullets.sagalib.processing.Organizer;
import com.codebullets.sagalib.processing.ReflectionInvoker;
import com.codebullets.sagalib.processing.SagaFactory;
import com.codebullets.sagalib.processing.SagaKeyReaderExtractor;
import com.codebullets.sagalib.processing.SagaMessageStream;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.AnnotationSagaAnalyzer;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Guice bindings for saga lib.
 */
class SagaLibModule extends AbstractModule {
    private Class<? extends StateStorage> stateStorage;
    private Class<? extends TimeoutManager> timeoutManager;
    private Class<? extends TypeScanner> scanner;
    private Class<? extends SagaProviderFactory> providerFactory;
    private List<Class<? extends Saga>> preferredOrder = new ArrayList<>();
    private Class<? extends ExecutionContext> executionContext;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindIfNotNull(StateStorage.class, stateStorage, Scopes.SINGLETON);
        bindIfNotNull(TimeoutManager.class, timeoutManager, Scopes.SINGLETON);
        bindIfNotNull(TypeScanner.class, scanner, Scopes.SINGLETON);
        bindIfNotNull(SagaProviderFactory.class, providerFactory, Scopes.SINGLETON);
        bindIfNotNull(ExecutionContext.class, executionContext);

        bind(SagaFactory.class).in(Singleton.class);
        bind(HandlerInvoker.class).to(ReflectionInvoker.class);
        bind(MessageStream.class).to(SagaMessageStream.class).in(Singleton.class);
        bind(SagaAnalyzer.class).to(AnnotationSagaAnalyzer.class).in(Singleton.class);
        bind(KeyExtractor.class).to(SagaKeyReaderExtractor.class).in(Singleton.class);
    }

    @Singleton
    @Provides
    private Organizer provideOrganizer(final SagaAnalyzer analyzer, final KeyExtractor extractor) {
        Organizer organizer = new Organizer(analyzer, extractor);
        organizer.setPreferredOrder(preferredOrder);

        return organizer;
    }

    /**
     * Perform binding to interface only if implementation type is not null.
     */
    private <T> void bindIfNotNull(final Class<T> interfaceType, @Nullable final Class<? extends T> implementationType) {
        if (implementationType != null) {
            bind(interfaceType).to(implementationType);
        }
    }

    /**
     * Perform binding to interface only if implementation type is not null.
     */
    private <T> void bindIfNotNull(final Class<T> interfaceType, @Nullable final Class<? extends T> implementationType, final Scope scope) {
        if (implementationType != null) {
            bind(interfaceType).to(implementationType).in(scope);
        }
    }

    /**
     * Sets the storage interface to use for saga state.
     */
    public void setStateStorage(@Nullable final Class<? extends StateStorage> stateStorage) {
        this.stateStorage = stateStorage;
    }

    /**
     * Sets the timeout manager to use.
     */
    public void setTimeoutManager(@Nullable final Class<? extends TimeoutManager> timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    /**
     * Sets the saga type scanner to use.
     */
    public void setScanner(@Nullable final Class<? extends TypeScanner> scanner) {
        this.scanner = scanner;
    }

    /**
     * Sets the saga instance provider factory.
     */
    public void setProviderFactory(@Nullable final Class<? extends SagaProviderFactory> providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * Sets the preferred execution order.
     */
    public void setExecutionOrder(final List<Class<? extends Saga>> executionOrder) {
        this.preferredOrder = executionOrder;
    }

    /**
     * Sets the execution context implementation.
     */
    public void setExecutionContext(final Class<? extends ExecutionContext> executionContext) {
        this.executionContext = executionContext;
    }
}