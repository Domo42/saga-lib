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

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.HandlerInvoker;
import com.codebullets.sagalib.processing.KeyExtractor;
import com.codebullets.sagalib.processing.Organizer;
import com.codebullets.sagalib.processing.ReflectionInvoker;
import com.codebullets.sagalib.processing.SagaFactory;
import com.codebullets.sagalib.processing.SagaKeyReaderExtractor;
import com.codebullets.sagalib.processing.SagaMessageStream;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.TimeoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a new instance of an {@link com.codebullets.sagalib.MessageStream} to run the saga lib.
 */
public final class EventStreamBuilder implements StreamBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(EventStreamBuilder.class);

    private final List<Class<? extends Saga>> preferredOrder = new ArrayList<>();
    private final Set<SagaModule> modules = new HashSet<>();
    private HandlerInvoker invoker;
    private SagaAnalyzer sagaAnalyzer;
    private TypeScanner scanner;
    private StateStorage storage;
    private SagaProviderFactory providerFactory;
    private TimeoutManager timeoutManager;
    private Provider<CurrentExecutionContext> contextProvider;

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
        buildSagaAnalyzer();
        buildInvoker();
        buildContextProvider();

        KeyExtractor extractor = new SagaKeyReaderExtractor(providerFactory);
        Organizer organizer = new Organizer(sagaAnalyzer, extractor);
        organizer.setPreferredOrder(preferredOrder);

        SagaFactory sagaFactory = new SagaFactory(providerFactory, storage, timeoutManager, organizer);

        return new SagaMessageStream(sagaFactory, invoker, storage, timeoutManager, contextProvider, modules);
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
        checkNotNull(contextProvider, "Context provider must be set.");

        contextProvider = provider;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FirstSagaToHandle defineHandlerExecutionOrder() {
        return new FirstSagaToHandle(preferredOrder, this);
    }

    @Override
    public StreamBuilder callingModule(final SagaModule module) {
        modules.add(module);
        return this;
    }

    private void buildTypeScanner() {
        if (scanner == null) {
            scanner = new ReflectionsTypeScanner();
        }
    }

    private void buildSagaAnalyzer() {
        if (sagaAnalyzer == null) {
            sagaAnalyzer = new AnnotationSagaAnalyzer(scanner);
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
            contextProvider = new Provider<CurrentExecutionContext>() {
                    @Override
                    public CurrentExecutionContext get() {
                        return new SagaExecutionContext();
                    }
                };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        tryClose(timeoutManager);
        tryClose(storage);
    }

    /**
     * Calls close if object implements {@link AutoCloseable} interface.
     */
    private void tryClose(final Object objectToClose) {
        try {
            if (objectToClose instanceof AutoCloseable) {
                AutoCloseable closeable = (AutoCloseable) objectToClose;
                closeable.close();
            }
        } catch (Exception ex) {
            LOG.error("Error closing object {}.", objectToClose, ex);
        }
    }
}
