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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.invocation.DefaultModuleCoordinator;
import com.codebullets.sagalib.processing.invocation.HandlerInvoker;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutExpired;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SagaMessageStream} class.
 */
public class SagaMessageStreamTest {
    private TimeoutManager timeoutManager;
    private StateStorage storage;
    private SagaMessageStream sut;
    private InstanceResolver instanceResolver;
    private HandlerInvoker invoker;
    private ExecutorService executorService;

    @Before
    public void init() {
        storage = mock(StateStorage.class);
        timeoutManager = mock(TimeoutManager.class);
        instanceResolver = mock(InstanceResolver.class);
        invoker = mock(HandlerInvoker.class);
        executorService = mock(ExecutorService.class);

        SagaEnvironment environment = SagaEnvironment.create(
                timeoutManager,
                storage,
                SagaExecutionContext::new, new HashSet<>(),
                new HashSet<>(),
                instanceResolver,
                DefaultModuleCoordinator::new);

        mockSagaCreation();
        sut = new SagaMessageStream(invoker, environment, executorService);
    }

    /**
     * Given => Do this always.
     * When  => Object is constructed.
     * Then  => Registers timeout callback listener.
     */
    @Test
    public void ctor_always_registerTimeoutCallback() {
        // given
        // when
        // then
        verify(timeoutManager).addExpiredCallback(isA(TimeoutExpired.class));
    }

    /**
     * <pre>
     * Given => do this always
     * When  => add is called
     * Then  => executes a saga on the provided executor service.
     * </pre>
     */
    @Test
    public void add_always_taskAddedToExecutor() {
        // given
        String message = "theMessage";

        // when
        sut.add(message);

        // then
        verify(executorService).execute(isA(SagaExecutionTask.class));
    }

    /**
     * <pre>
     * Given => Do this always.
     * When  => handle is called
     * Then  => Saga is directly executed.
     * </pre>
     */
    @Test
    public void handle_always_invokesSagaRightAway() throws InvocationTargetException, IllegalAccessException {
        // given
        String message = "theMessage";

        // when
        sut.handle(message);

        // then
        verify(invoker).invoke(isA(Saga.class), same(message));
    }

    @Test
    public void close_always_executorShutdown() {
        // when
        sut.close();

        // then
        verify(executorService).shutdown();
    }

    private void mockSagaCreation() {
        SagaInstanceInfo saga = new SagaInstanceInfo(mock(Saga.class), true);
        when(instanceResolver.resolve(any(LookupContext.class))).thenReturn(Lists.newArrayList(saga));
    }
}