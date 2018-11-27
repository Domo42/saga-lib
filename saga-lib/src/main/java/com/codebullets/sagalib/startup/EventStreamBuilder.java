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

import com.codebullets.sagalib.AutoCloseables;
import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.DefaultStrategyFinder;
import com.codebullets.sagalib.processing.invocation.DefaultModuleCoordinator;
import com.codebullets.sagalib.processing.invocation.HandlerInvoker;
import com.codebullets.sagalib.processing.KeyExtractor;
import com.codebullets.sagalib.processing.invocation.ModuleCoordinatorFactory;
import com.codebullets.sagalib.processing.invocation.ReflectionInvoker;
import com.codebullets.sagalib.processing.SagaEnvironment;
import com.codebullets.sagalib.processing.SagaInstanceCreator;
import com.codebullets.sagalib.processing.SagaInstanceFactory;
import com.codebullets.sagalib.processing.SagaKeyReaderExtractor;
import com.codebullets.sagalib.processing.SagaMessageStream;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.processing.StrategyInstanceResolver;
import com.codebullets.sagalib.processing.TypesForMessageMapper;
import com.codebullets.sagalib.storage.MemoryStorage;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.TimeoutManager;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a new instance of an {@link com.codebullets.sagalib.MessageStream} to run the saga lib.
 */
public final class EventStreamBuilder implements StreamBuilder {
    private final List<Class<? extends Saga>> preferredOrder = new ArrayList<>();
    private final Set<SagaModule> modules = new HashSet<>();
    private final Set<SagaLifetimeInterceptor> interceptors = new HashSet<>();
    private HandlerInvoker invoker;
    private SagaAnalyzer sagaAnalyzer;
    private TypeScanner scanner;
    private StateStorage storage;
    private SagaProviderFactory providerFactory;
    private TimeoutManager timeoutManager;
    private Provider<CurrentExecutionContext> contextProvider;
    private Executor executor;
    private SagaMessageStream messageStream;
    private final Collection<Class<? extends Annotation>> startSagaAnnotations = new ArrayList<>();
    private final Collection<Class<? extends Annotation>> handlerAnnotations = new ArrayList<>();
    private ModuleCoordinatorFactory moduleCoordinatorFactory;

    /**
     * Prevent instantiation from outside. Use {@link #configure()} instead.
     */
    private EventStreamBuilder() {
    }

    /**
     * Start configuration and creation of the saga lib event stream.
     */
    public static StreamBuilder configure() {
        return new EventStreamBuilder();
    }

    @Override
    public MessageStream build() {
        if (providerFactory == null) {
            throw new UnsupportedOperationException("The saga provider factory has to be set to build the event stream.");
        }

        buildTypeScanner();
        buildTimeoutManager();
        SagaInstanceCreator instanceCreator = new SagaInstanceCreator(providerFactory, timeoutManager);

        buildSagaAnalyzer(instanceCreator);
        buildInvoker();
        buildContextProvider();
        buildExecutor();
        buildStorage();
        buildModuleCoordinatorFactory();

        SagaInstanceFactory instanceFactory = new SagaInstanceFactory(instanceCreator);
        TypesForMessageMapper messageMapper = new TypesForMessageMapper(sagaAnalyzer);
        messageMapper.setPreferredOrder(preferredOrder);

        KeyExtractor extractor = new SagaKeyReaderExtractor(providerFactory);
        DefaultStrategyFinder strategyFinder = new DefaultStrategyFinder(messageMapper, instanceFactory, extractor, storage);
        StrategyInstanceResolver instanceResolver = new StrategyInstanceResolver(strategyFinder);

        SagaEnvironment environment = SagaEnvironment.create(
                timeoutManager,
                storage,
                contextProvider,
                modules,
                interceptors,
                instanceResolver,
                moduleCoordinatorFactory);

        messageStream = new SagaMessageStream(invoker, environment, executor);
        return messageStream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingScanner(final TypeScanner typeScanner) {
        checkNotNull(typeScanner, "Scanner to use must not be null.");

        this.scanner = typeScanner;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingStorage(final StateStorage stateStorage) {
        checkNotNull(stateStorage, "Storage to use must not be null.");

        this.storage = stateStorage;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingTimeoutManager(final TimeoutManager manager) {
        checkNotNull(manager, "Timeout manager must not be null.");

        this.timeoutManager = manager;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingSagaProviderFactory(final SagaProviderFactory sagaProviderFactory) {
        checkNotNull(sagaProviderFactory, "Provider factory must be set.");

        providerFactory = sagaProviderFactory;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingContextProvider(final Provider<CurrentExecutionContext> provider) {
        checkNotNull(provider, "Context provider must be set.");

        contextProvider = provider;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder usingExecutor(final Executor executorService) {
        executor = executorService;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FirstSagaToHandle defineHandlerExecutionOrder() {
        return new FirstSagaToHandle(preferredOrder, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder callingModule(final SagaModule module) {
        modules.add(module);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamBuilder callingInterceptor(final SagaLifetimeInterceptor interceptor) {
        interceptors.add(interceptor);
        return this;
    }

    @Override
    public StreamBuilder addStartSagaAnnotation(final Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(annotationClass, "The type of annotation is not allowed to be null");
        startSagaAnnotations.add(annotationClass);
        return this;
    }

    @Override
    public StreamBuilder addHandlerAnnotation(final Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(annotationClass, "The type of annotation is not allowed to be null");
        handlerAnnotations.add(annotationClass);
        return this;
    }

    @Override
    public StreamBuilder usingModuleCoordinator(final ModuleCoordinatorFactory coordinatorFactory) {
        moduleCoordinatorFactory = coordinatorFactory;
        return this;
    }

    private void buildTypeScanner() {
        if (scanner == null) {
            scanner = new ReflectionsTypeScanner();
        }
    }

    private void buildStorage() {
        if (storage == null) {
            storage = new MemoryStorage();
        }
    }

    private void buildModuleCoordinatorFactory() {
        if (moduleCoordinatorFactory == null) {
            moduleCoordinatorFactory = DefaultModuleCoordinator::new;
        }
    }

    private void buildSagaAnalyzer(final SagaInstanceCreator instanceCreator) {
        if (sagaAnalyzer == null) {
            AnnotationSagaAnalyzer annotationSagaAnalyzer = new AnnotationSagaAnalyzer(scanner);
            startSagaAnnotations.forEach(annotationSagaAnalyzer::addStartSagaAnnotation);
            handlerAnnotations.forEach(annotationSagaAnalyzer::addHandlerAnnotation);

            HandlerDescriptionAnalyzer handlerDescriptionAnalyzer = new HandlerDescriptionAnalyzer(scanner, instanceCreator);
            sagaAnalyzer = new CombinedSagaAnalyzer(annotationSagaAnalyzer, handlerDescriptionAnalyzer);
        }
    }

    private void buildInvoker() {
        if (invoker == null) {
            invoker = new ReflectionInvoker(sagaAnalyzer);
        }
    }

    private void buildTimeoutManager() {
        if (timeoutManager == null) {
            timeoutManager = new InMemoryTimeoutManager();
        }
    }

    private void buildContextProvider() {
        if (contextProvider == null) {
            contextProvider = SagaExecutionContext::new;
        }
    }

    private void buildExecutor() {
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor(
                    r -> {
                        Thread thread = new Thread(r, "saga-lib");
                        thread.setDaemon(true);
                        return thread;
                    }
            );
        }
    }

    @Override
    public void close() {
        AutoCloseables.closeQuietly(messageStream);
    }
}
