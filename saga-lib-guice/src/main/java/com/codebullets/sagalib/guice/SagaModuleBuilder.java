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

import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.ReflectionsTypeScanner;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.storage.MemoryStorage;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.inject.Module;

/**
 * Creates a Guice module to bind all saga lib dependencies.
 * Use this module when create a Guice Injector instance.<p/>
 * This enables the caller to retrieve a new message stream instance
 * directly from Guice. If none of the 'use' methods are called
 * the saga-lib will use default implementations.<p/>
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

    /**
     * Prevent direct instance creation of class.
     */
    private SagaModuleBuilder() {
        // set default implementations
        stateStorage = MemoryStorage.class;
        timeoutMgr = InMemoryTimeoutManager.class;
        scanner = ReflectionsTypeScanner.class;
        providerFactory = GuiceSagaProviderFactory.class;
    }

    /**
     * Start configuration of saga guice module.
     */
    public static SagaModuleBuilder configure() {
        return new SagaModuleBuilder();
    }

    /**
     * Set the class to use to save saga state. If not called all saga state will be
     * stored in memory.
     */
    public SagaModuleBuilder useStateStorage(final Class<? extends StateStorage> stateStorageClass) {
        this.stateStorage = stateStorageClass;
        return this;
    }

    /**
     * Sets the class to use for timeout management. If not called the saga timeouts will
     * be triggered by JVM timers and not be persisted.
     */
    public SagaModuleBuilder useTimeoutManager(final Class<? extends TimeoutManager> timeoutMgrClass) {
        this.timeoutMgr = timeoutMgrClass;
        return this;
    }

    /**
     * Sets the scanner to use searching for available saga classes. If not called the
     * lib will search for all available classes in the classpath.
     */
    public SagaModuleBuilder useSagaScanner(final Class<? extends TypeScanner> scannerClass) {
        this.scanner = scannerClass;
        return this;
    }

    /**
     * Use custom provider factory for individual saga creation. If not called the lib
     * will use Guice to create saga instance providers.
     */
    public SagaModuleBuilder useProviderFactory(final Class<? extends SagaProviderFactory> providerFactoryClass) {
        providerFactory = providerFactoryClass;
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

        return module;
    }
}