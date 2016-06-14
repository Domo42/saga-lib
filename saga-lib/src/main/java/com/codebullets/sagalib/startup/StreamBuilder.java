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
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;

import javax.inject.Provider;
import java.util.concurrent.Executor;

/**
 * Configures and builds a new saga event stream.
 */
public interface StreamBuilder extends AutoCloseable {
    /**
     * Creates a new message stream instance.
     */
    MessageStream build();

    /**
     * Optional: Sets the type scanner to use. If not set the saga lib
     * will check the whole class path for implementations of sagas.
     * @param scanner The saga type scanner to use.
     */
    StreamBuilder usingScanner(TypeScanner scanner);

    /**
     * Optional: Sets the storage interface used to save and retrieve the saga state instance.
     * If not set all states will be stored in local memory.
     * @param storage The storage engine to use.
     */
    StreamBuilder usingStorage(StateStorage storage);

    /**
     * Optional: Sets the manager responsible to collect and trigger timeouts. If
     * not set timeouts will be persisted in memory and triggered by JVM timers.
     */
    StreamBuilder usingTimeoutManager(TimeoutManager timeoutManager);

    /**
     * Must be set. A factory returning JSR-330 providers for saga instances.
     * The returned sagas are expected to be new instances all the time. The state
     * of a saga is attached to the instance and as such not thread safe. To avoid
     * this it is best to always return a new instance if you are not absolutely sure
     * the sagas are always handled from the same thread.
     */
    StreamBuilder usingSagaProviderFactory(SagaProviderFactory providerFactory);

    /**
     * Optional: Sets a provider able to create a new execution context. A single execution
     * context is shared across all handles and saga of a single message. If no custom
     * provider is set the saga-lib will create a new execution context for every message to
     * separate the instance when messages are handled from different threads.
     */
    StreamBuilder usingContextProvider(Provider<CurrentExecutionContext> contextProvider);

    /**
     * <p>Optional: Sets the executor to use for asynchronous handling. This one is
     * used when calling {@link com.codebullets.sagalib.MessageStream#add(Object)} to trigger
     * saga execution. No executor is used for synchronous {@link com.codebullets.sagalib.MessageStream#handle(Object)}
     * message handling.</p>
     *
     * <p>If no custom executor is provided a single background thread is used to process all
     * messages.</p>
     */
    StreamBuilder usingExecutor(final Executor executorService);

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
    FirstSagaToHandle defineHandlerExecutionOrder();

    /**
     * <p>Adds a module to be called before and after a message is handled by one or
     * more saga implementations.</p>
     *
     * <p>There can be multiple modules. The order in which the modules are
     * executed is not defined.</p>
     */
    StreamBuilder callingModule(final SagaModule module);

    /**
     * Adds a lifetime interceptor that will be called every time an individual saga is started and finished.
     *
     * <p>It is possible to add multiple interceptors.</p>
     */
    StreamBuilder callingInterceptor(final SagaLifetimeInterceptor interceptor);
}
