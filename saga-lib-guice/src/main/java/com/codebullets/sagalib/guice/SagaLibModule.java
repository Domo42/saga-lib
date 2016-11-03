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

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.processing.HandlerInvoker;
import com.codebullets.sagalib.processing.InstanceResolver;
import com.codebullets.sagalib.processing.KeyExtractor;
import com.codebullets.sagalib.processing.SagaInstanceCreator;
import com.codebullets.sagalib.processing.SagaInstanceFactory;
import com.codebullets.sagalib.processing.SagaKeyReaderExtractor;
import com.codebullets.sagalib.processing.SagaMessageStream;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.processing.StrategyFinder;
import com.codebullets.sagalib.processing.StrategyInstanceResolver;
import com.codebullets.sagalib.processing.TypesForMessageMapper;
import com.codebullets.sagalib.startup.*;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Guice bindings for saga lib.
 */
class SagaLibModule extends AbstractModule {
    private Class<? extends StateStorage> stateStorage;
    private Class<? extends TimeoutManager> timeoutManager;
    private Class<? extends TypeScanner> scanner;
    private Class<? extends SagaProviderFactory> providerFactory;
    private Class<? extends StrategyFinder> strategyFinder;
    private List<Class<? extends Saga>> preferredOrder = new ArrayList<>();
    private Collection<Class<? extends SagaModule>> moduleTypes = new ArrayList<>();
    private Collection<Class<? extends SagaLifetimeInterceptor>> interceptorTypes = new ArrayList<>();
    private Class<? extends CurrentExecutionContext> executionContext;
    private Executor executor;
    private Class<? extends HandlerInvoker> invoker;
    private Collection<Class<? extends Annotation>> startSagaAnnotations = new ArrayList<>();
    private Collection<Class<? extends Annotation>> handlerAnnotations = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        bindIfNotNull(StateStorage.class, stateStorage, Scopes.SINGLETON);
        bindIfNotNull(TimeoutManager.class, timeoutManager, Scopes.SINGLETON);
        bindIfNotNull(TypeScanner.class, scanner, Scopes.SINGLETON);
        bindIfNotNull(SagaProviderFactory.class, providerFactory, Scopes.SINGLETON);
        bindIfNotNull(StrategyFinder.class, strategyFinder, Scopes.SINGLETON);
        bindIfNotNull(HandlerInvoker.class, invoker, Scopes.SINGLETON);

        bindIfNotNull(CurrentExecutionContext.class, executionContext);
        bind(ExecutionContext.class).toProvider(binder().getProvider(CurrentExecutionContext.class));

        bind(SagaInstanceCreator.class).in(Singleton.class);
        bind(SagaInstanceFactory.class).in(Singleton.class);
        bind(InstanceResolver.class).to(StrategyInstanceResolver.class).in(Singleton.class);
        bind(MessageStream.class).to(SagaMessageStream.class).in(Singleton.class);
        bind(KeyExtractor.class).to(SagaKeyReaderExtractor.class).in(Singleton.class);

        bindModules();
        bindExecutor();
        bindInterceptors();
    }

    private void bindInterceptors() {
        Multibinder<SagaLifetimeInterceptor> multiBinder = Multibinder.newSetBinder(binder(), SagaLifetimeInterceptor.class);
        for (Class<? extends SagaLifetimeInterceptor> interceptorType : interceptorTypes) {
            multiBinder.addBinding().to(interceptorType).in(Singleton.class);
        }
    }

    private void bindExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "saga-lib");
                        thread.setDaemon(true);
                        return thread;
                    }
            );
        }

        bind(Executor.class).toInstance(executor);
    }

    private void bindModules() {
        Multibinder<SagaModule> moduleBinder = Multibinder.newSetBinder(binder(), SagaModule.class);
        for (Class<? extends  SagaModule> moduleType : moduleTypes) {
            moduleBinder.addBinding().to(moduleType).in(Singleton.class);
        }
    }

    @Singleton
    @Provides
    private SagaAnalyzer provide(final TypeScanner typeScanner, final DirectDescriptionAnalyzer directAnalyzer) {
        AnnotationSagaAnalyzer annotationAnalyzer = new AnnotationSagaAnalyzer(typeScanner);

        startSagaAnnotations.forEach(annotationAnalyzer::addStartSagaAnnotation);
        handlerAnnotations.forEach(annotationAnalyzer::addHandlerAnnotation);

        return new CombinedSagaAnalyzer(annotationAnalyzer, directAnalyzer);
    }

    @Singleton
    @Provides
    private TypesForMessageMapper provide(final SagaAnalyzer analyzer) {
        TypesForMessageMapper mapper = new TypesForMessageMapper(analyzer);
        mapper.setPreferredOrder(preferredOrder);

        return mapper;
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
    public void setExecutionContext(final Class<? extends CurrentExecutionContext> executionContext) {
        this.executionContext = executionContext;
    }

    /**
     * Sets the list of available modules.
     */
    public void setModuleTypes(final Collection<Class<? extends SagaModule>> moduleTypes) {
        this.moduleTypes = moduleTypes;
    }

    /**
     * Set the list of known interceptors.
     */
    public void setInterceptorTypes(final Collection<Class<? extends SagaLifetimeInterceptor>> interceptorTypes) {
        this.interceptorTypes = interceptorTypes;
    }

    /**
     * Sets the executor for async operations.
     */
    public void setExecutor(final Executor executor) {
        this.executor = executor;
    }

    /**
     * Sets the type of strategy finder to use.
     */
    public void setStrategyFinder(final Class<? extends StrategyFinder> strategyFinderType) {
        this.strategyFinder = strategyFinderType;
    }

    /**
     * Sets the type of the invoker to use.
     */
    public void setInvoker(final Class<? extends HandlerInvoker> invokerType) {
        this.invoker = invokerType;
    }

    /**
     * Sets a list of additional starting saga annotations to use.
     */
    public void setStartSagaAnnotations(final Collection<Class<? extends Annotation>> startSagaAnnotations) {
        this.startSagaAnnotations = startSagaAnnotations;
    }

    /**
     * Sets a list of additional starting handler annotations to use.
     */
    public void setHandlerAnnotations(final Collection<Class<? extends Annotation>> handlerAnnotations) {
        this.handlerAnnotations = handlerAnnotations;
    }
}