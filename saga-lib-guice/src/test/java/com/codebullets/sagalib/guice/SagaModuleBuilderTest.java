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
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests for {@link SagaModuleBuilder} class.
 */
public class SagaModuleBuilderTest {
    /**
     * Given => Guice injector created with saga lib defaults.
     * When  => getInstance is called.
     * Then  => Guice returns message stream instances.
     */
    @Test
    public void getInstance_defaultSettings_guiceReturnsMessageStream() {
        // given
        Module module = SagaModuleBuilder.configure().build();
        Injector injector = Guice.createInjector(module);

        // when
        MessageStream msgStream = injector.getInstance(MessageStream.class);

        // then
        assertThat("Message stream should not be null.", msgStream, not(nullValue()));
    }

    /**
     * Given => Saga lib created with default settings.
     * When  => String message is added to msg stream.
     * Then  => Monitor reports saga as started.
     */
    @Test
    public void handleString_defaultSettings_sagaHasBeenStarted() throws InvocationTargetException, IllegalAccessException {
        // given
        Module sagaModule = SagaModuleBuilder.configure().build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        MessageStream msgStream = injector.getInstance(MessageStream.class);

        // when
        msgStream.handle("anyString");

        // then
        SagaMonitor monitor = injector.getInstance(SagaMonitor.class);
        assertThat("Expected saga to be started.", monitor.getSagaHasStarted(), equalTo(true));
    }

    /**
     * <pre>
     * Given => Module is configured for execution.
     * When  => String message is added msg stream
     * Then  => Monitor reports start of module.
     * </pre>
     */
    @Test
    public void handleString_moduleIsConfigured_moduleStartHasBeenExecuted() throws InvocationTargetException, IllegalAccessException {
        // given
        Module sagaModule = SagaModuleBuilder.configure().callModule(TestSagaModule.class).build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        MessageStream msgStream = injector.getInstance(MessageStream.class);

        // when
        msgStream.handle("anyString");

        // then
        SagaMonitor monitor = injector.getInstance(SagaMonitor.class);
        assertThat("Expected saga to be started.", monitor.hasModuleStarted(), equalTo(true));
    }

    /**
     * <pre>
     * Given => Execution context configuration.
     * When  => Execution context is requested twice.
     * Then  => Returns different instances.
     * </pre>
     */
    @Test
    public void getInstance_executionContext_alwaysReturnNewContext() {
        // given
        Module sagaModule = SagaModuleBuilder.configure().build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        ExecutionContextConsumer contextConsumer = injector.getInstance(ExecutionContextConsumer.class);
        ExecutionContext context1 = contextConsumer.newContext();

        // when
        ExecutionContext context2 = injector.getInstance(ExecutionContext.class);

        // then
        assertThat("Expected two different instances.", context2, not(sameInstance(context1)));
    }

    /**
     * <pre>
     * Given => Module is configured.
     * When  => String message is added to stream
     * Then  => Monitor reports saga has been started.
     * </pre>
     */
    @Test
    public void handleString_sagaHandledAsynchronous_isExecutedInBackground() throws InterruptedException {
        // given
        Module sagaModule = SagaModuleBuilder.configure().callModule(TestSagaModule.class).build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        MessageStream msgStream = injector.getInstance(MessageStream.class);

        // when
        msgStream.add("anyString");

        // then
        SagaMonitor monitor = injector.getInstance(SagaMonitor.class);
        boolean waitSucceeded = monitor.waitForSagaStarted(2, TimeUnit.SECONDS);

        assertThat("Expected saga to be executed.", waitSucceeded, equalTo(true));
    }

    /**
     * <pre>
     * Given => Custom interceptor is configured
     * When  => String message is handled
     * Then  => Interceptor has been called
     * </pre>
     */
    @Test
    public void handleString_interceptorConfigured_interceptorIsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        Module sagaModule = SagaModuleBuilder.configure().callInterceptor(CustomInterceptor.class).build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        MessageStream msgStream = injector.getInstance(MessageStream.class);
        Set<SagaLifetimeInterceptor> interceptors = injector.getInstance(Key.get(new TypeLiteral<Set<SagaLifetimeInterceptor>>() {}));

        // when
        msgStream.handle("anyString");

        // then
        CustomInterceptor interceptor = (CustomInterceptor) interceptors.iterator().next();
        assertThat("Expected interceptor to be called.", interceptor.hasStartingBeenCalled(), equalTo(true));
    }

    /**
     * <pre>
     * Given => Custom interceptor is configured
     * When  => String message is handled
     * Then  => Interceptor handling has been called
     * </pre>
     */
    @Test
    public void handleString_interceptorConfigured_interceptorHandlingIsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        Module sagaModule = SagaModuleBuilder.configure().callInterceptor(CustomInterceptor.class).build();
        Injector injector = Guice.createInjector(sagaModule, new CustomModule());
        MessageStream msgStream = injector.getInstance(MessageStream.class);
        Set<SagaLifetimeInterceptor> interceptors = injector.getInstance(Key.get(new TypeLiteral<Set<SagaLifetimeInterceptor>>() {}));

        // when
        msgStream.handle("anyString");

        // then
        CustomInterceptor interceptor = (CustomInterceptor) interceptors.iterator().next();
        assertThat("Expected interceptor executing to be called.", interceptor.hasExecutingBeenCalled(), equalTo(true));
    }
}