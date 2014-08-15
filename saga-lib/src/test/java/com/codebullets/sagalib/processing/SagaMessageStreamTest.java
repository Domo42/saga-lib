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

import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutExpired;
import com.codebullets.sagalib.timeout.TimeoutManager;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link SagaMessageStream} class.
 */
public class SagaMessageStreamTest {
    private TimeoutManager timeoutManager;
    private StateStorage storage;
    private SagaMessageStream sut;
    private SagaFactory factory;
    private HandlerInvoker invoker;

    @Before
    public void init() {
        storage = mock(StateStorage.class);
        timeoutManager = mock(TimeoutManager.class);
        factory = mock(SagaFactory.class);
        invoker = mock(HandlerInvoker.class);

        SagaEnvironment environment = SagaEnvironment.create(
                timeoutManager, storage, factory, null, new HashSet<SagaModule>(),
                new HashSet<SagaLifetimeInterceptor>());

        sut = new SagaMessageStream(invoker, environment, mock(ExecutorService.class));
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
}