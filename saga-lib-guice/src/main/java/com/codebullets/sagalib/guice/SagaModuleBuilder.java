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
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.DefaultStrategyFinder;
import com.codebullets.sagalib.processing.HandlerInvoker;
import com.codebullets.sagalib.processing.ReflectionInvoker;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.processing.StrategyFinder;
import com.codebullets.sagalib.startup.ReflectionsTypeScanner;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.storage.MemoryStorage;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.inject.Module;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * <p>Creates a Guice module to bind all saga lib dependencies.
 * Use this module when create a Guice Injector instance.</p>
 *
 * <p>This enables the caller to retrieve a new message stream instance
 * directly from Guice. If none of the 'use' methods are called
 * the saga-lib will use default implementations.</p>
 *
 * <strong>Example: </strong> showing lib creation using as custom state storage implementation. In a real
 * application the {@code createInjector} calls will most likely be called additional app specific
 * module parameters.
 * <pre><code>
 * Module sagaLibModule = SagaModuleBuilder().configure()
 *                           .useStateStorage(MyDatabaseStateStorage.class)
 *                           .build();
 * Injector injector = Guice.createInjector(sagaLibModule);
 *
 * MessageStream msgStream = injector.getInstance(MessageStream.class);
 * </code>
 * </pre>
 */
public final class SagaModuleBuilder {
    private Class<? extends StateStorage> stateStorage;
    private Class<? extends TimeoutManager> timeoutMgr;
    private Class<? extends TypeScanner> scanner;
    private Class<? extends SagaProviderFactory> providerFactory;
    private Class<? extends CurrentExecutionContext> executionContext;
    private Class<? extends StrategyFinder> strategyFinder;
    private Class<? extends HandlerInvoker> invoker;
    private final List<Class<? extends Saga>> preferredOrder = new ArrayList<>();
    private final Collection<Class<? extends SagaModule>> moduleTypes = new ArrayList<>();
    private final Collection<Class<? extends SagaLifetimeInterceptor>> interceptorTypes = new ArrayList<>();
    private final Collection<Class<? extends Annotation>> startSagaAnnotations = new ArrayList<>();
    private final Collection<Class<? extends Annotation>> handlerAnnotations = new ArrayList<>();
    private Executor executor;

    /**
     * Prevent direct instance creation of class.
     */
    private SagaModuleBuilder() {
        // set default implementations
        stateStorage = MemoryStorage.class;
        timeoutMgr = InMemoryTimeoutManager.class;
        scanner = ReflectionsTypeScanner.class;
        providerFactory = GuiceSagaProviderFactory.class;
        executionContext = SagaExecutionContext.class;
        strategyFinder = DefaultStrategyFinder.class;
        invoker = ReflectionInvoker.class;
    }

    /**
     * Start configuration of saga guice module.
     */
    public static SagaModuleBuilder configure() {
        return new SagaModuleBuilder();
    }

    /**
     * Set the class to use to save saga state. If not called all saga state will be
     * stored in memory. If null clears the default implementation.
     */
    public SagaModuleBuilder useStateStorage(@Nullable final Class<? extends StateStorage> stateStorageClass) {
        this.stateStorage = stateStorageClass;
        return this;
    }

    /**
     * Clears the default state storage implementation binding.
     */
    public SagaModuleBuilder clearStateStorageBinding() {
        this.stateStorage = null;
        return this;
    }

    /**
     * Sets the class to use for timeout management. If not called the saga timeouts will
     * be triggered by JVM timers and not be persisted. If null clears the default binding implementation.
     */
    public SagaModuleBuilder useTimeoutManager(@Nullable final Class<? extends TimeoutManager> timeoutMgrClass) {
        this.timeoutMgr = timeoutMgrClass;
        return this;
    }

    /**
     * Clears the default timeout manager binding.
     */
    public SagaModuleBuilder clearTimeoutManagerBinding() {
        this.timeoutMgr = null;
        return this;
    }

    /**
     * Sets the scanner to use searching for available saga classes. If not called the
     * lib will search for all available classes in the classpath. If null clears the default binding implementation.
     */
    public SagaModuleBuilder useSagaScanner(@Nullable final Class<? extends TypeScanner> scannerClass) {
        this.scanner = scannerClass;
        return this;
    }

    /**
     * Clears the default scanner binding.
     */
    public SagaModuleBuilder clearSagaScannerBinding() {
        this.scanner = null;
        return this;
    }

    /**
     * Use custom provider factory for individual saga creation. If not called the lib
     * will use Guice to create saga instance providers. If null clears the default binding implementation.
     */
    public SagaModuleBuilder useProviderFactory(@Nullable final Class<? extends SagaProviderFactory> providerFactoryClass) {
        providerFactory = providerFactoryClass;
        return this;
    }

    /**
     * Clears the default instance provider factory binding.
     */
    public SagaModuleBuilder clearProviderFactory() {
        providerFactory = null;
        return this;
    }

    /**
     * Use custom strategy finder. This allows control to how to resolve saga instances
     * for specific message types.
     */
    public SagaModuleBuilder useStrategyFinder(@Nullable final Class<? extends StrategyFinder> strategyFinderClass) {
        strategyFinder = strategyFinderClass;
        return this;
    }

    /**
     * Clears the default implementation of the {@link StrategyFinder} used.
     */
    public SagaModuleBuilder clearStrategyFinder() {
        strategyFinder = null;
        return this;
    }

    /**
     * Use custom implementation for {@link CurrentExecutionContext} interface.
     */
    public SagaModuleBuilder useExecutionContext(@Nullable final Class<? extends CurrentExecutionContext> contextClass) {
        executionContext = contextClass;
        return this;
    }

    /**
     * Clears the default implementation of {@link CurrentExecutionContext} used.
     */
    public SagaModuleBuilder clearExecutionContext() {
        executionContext = null;
        return this;
    }

    /**
     * Define the class responsible to call the actual handler methods.
     */
    public SagaModuleBuilder useInvoker(@Nullable final Class<? extends HandlerInvoker> invokerClass) {
        invoker = invokerClass;
        return this;
    }

    /**
     * Clears the default implementation of the invoker class used.
     */
    public SagaModuleBuilder clearInvoker() {
        invoker = null;
        return this;
    }

    /**
     * <p>Defines the order of saga message handlers in case a message is associated with multiple
     * saga types by either {@literal @}StartsSaga or {@literal @}EventHandler.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>builder.defineHandlerExecutionOrder()
     *          .firstExecute(FirstSagaToExecute.class)
     *          .then(SecondToExecute.class)
     *          .then(OtherSaga.class)
     * </pre>
     */
    public FirstSagaToHandle defineHandlerExecutionOrder() {
        return new FirstSagaToHandle(preferredOrder, this);
    }

    /**
     * Adds a module to be called before and after a message is handled by one or more sagas.
     */
    public SagaModuleBuilder callModule(final Class<? extends SagaModule> module) {
        moduleTypes.add(module);
        return this;
    }

    /**
     * Adds a lifetime interceptor that will be called every time an individual saga is started and finished.
     */
    public SagaModuleBuilder callInterceptor(final Class<? extends SagaLifetimeInterceptor> interceptor) {
        interceptorTypes.add(interceptor);
        return this;
    }

    /**
     * Optional: Sets the executor to use for asynchronous handling. This one is
     * used when calling {@link com.codebullets.sagalib.MessageStream#add(Object)} to trigger
     * saga execution. No executor is used for synchronous {@link com.codebullets.sagalib.MessageStream#handle(Object)}
     * message handling.
     * <p>If no custom executor is provided a single background thread is used to process all
     * messages.</p>
     */
    public SagaModuleBuilder usingExecutor(final Executor executorService) {
        executor = executorService;
        return this;
    }

    /**
     * Adds a custom annotation to be used when scanning for methods
     * starting a new saga. By default the {@link com.codebullets.sagalib.StartsSaga} annotation
     * will be used.
     */
    public SagaModuleBuilder addStartSagaAnnotation(final Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(annotationClass, "The type of annotation is not allowed to be null");
        startSagaAnnotations.add(annotationClass);
        return this;
    }

    /**
     * Adds a custom annotation to be used when scanning for handler methods
     * to continue an existing saga. By default the {@link com.codebullets.sagalib.EventHandler} annotation
     * will be used.
     */
    public SagaModuleBuilder addHandlerAnnotation(final Class<? extends Annotation> annotationClass) {
        Objects.requireNonNull(annotationClass, "The type of annotation is not allowed to be null");
        handlerAnnotations.add(annotationClass);
        return this;
    }

    /**
     * Creates the module containing all saga lib bindings.
     */
    public Module build() {
        SagaLibModule module = new SagaLibModule();
        module.setStateStorage(stateStorage);
        module.setTimeoutManager(timeoutMgr);
        module.setScanner(scanner);
        module.setProviderFactory(providerFactory);
        module.setExecutionOrder(preferredOrder);
        module.setExecutionContext(executionContext);
        module.setModuleTypes(moduleTypes);
        module.setExecutor(executor);
        module.setInterceptorTypes(interceptorTypes);
        module.setStrategyFinder(strategyFinder);
        module.setInvoker(invoker);

        return module;
    }
}