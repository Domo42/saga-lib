/*
 * Copyright 2016 Stefan Domnanovits
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.processing.invocation.ModulesInvoker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ModulesInvokerTest {
    private List<SagaModule> modules;

    @Before
    public void initModulesInvokerTest() {
        modules = new ArrayList<>();
        modules.add(mock(SagaModule.class));
        modules.add(mock(SagaModule.class));
        modules.add(mock(SagaModule.class));
    }

    @Test
    public void start_listOfModules_callsStartOnAllModules() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);

        // when
        ModulesInvoker.start(context, modules);

        // then
        verify(modules.get(0)).onStart(context);
        verify(modules.get(1)).onStart(context);
        verify(modules.get(2)).onStart(context);
    }

    @Test
    public void finish_allModulesStarted_callsFinishedOnAllModulesInReverseOrder() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        ModulesInvoker sut = ModulesInvoker.start(context, modules);

        // when
        sut.finish();

        // then
        InOrder inOrder = inOrder(modules.get(0), modules.get(1), modules.get(2));
        inOrder.verify(modules.get(2)).onFinished(context);
        inOrder.verify(modules.get(1)).onFinished(context);
        inOrder.verify(modules.get(0)).onFinished(context);
    }

    @Test
    public void start_startErrorOnSecondModule_callsFinishedForSecondThenFirst() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        NullPointerException ex = new NullPointerException();
        doThrow(ex).when(modules.get(1)).onStart(context);

        // given
        try {
            ModulesInvoker.start(context, modules);
        } catch (Exception e) {
            // is expected, module start is exception neutral
        }

        // then
        InOrder inOrder = inOrder(modules.get(0), modules.get(1));
        inOrder.verify(modules.get(1)).onFinished(context);
        inOrder.verify(modules.get(0)).onFinished(context);
    }

    @Test
    public void start_startErrorOnSecondModule_doNotStartOrFinishedThird() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        NullPointerException ex = new NullPointerException();
        doThrow(ex).when(modules.get(1)).onStart(context);

        // when
        try {
            ModulesInvoker.start(context, modules);
        } catch (Exception e) {
            // is expected, module start() is exception neutral
        }

        // then
        verify(modules.get(2), never()).onStart(context);
        verify(modules.get(2), never()).onFinished(context);
    }

    @Test
    public void error_allModulesStarted_callsErrorOnAllModulesInReverseOrder() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        ModulesInvoker sut = ModulesInvoker.start(context, modules);

        Object message = new Object();
        NullPointerException ex = new NullPointerException();

        // when
        sut.error(message, ex);

        // then
        InOrder inOrder = inOrder(modules.get(0), modules.get(1), modules.get(2));
        inOrder.verify(modules.get(2)).onError(context, message, ex);
        inOrder.verify(modules.get(1)).onError(context, message, ex);
        inOrder.verify(modules.get(0)).onError(context, message, ex);
    }

    @Test
    public void start_exceptionOnSecond_callsErrorOnSecondThenFirst() {
        // given
        Object message = new Object();
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        when(context.message()).thenReturn(message);

        NullPointerException ex = new NullPointerException();
        doThrow(ex).when(modules.get(1)).onStart(context);

        // when
        try {
            ModulesInvoker.start(context, modules);
        } catch (Exception e) {
            // expected
        }

        // then
        InOrder inOrder = inOrder(modules.get(0), modules.get(1));
        inOrder.verify(modules.get(1)).onError(context, message, ex);
        inOrder.verify(modules.get(0)).onError(context, message, ex);
    }

    @Test
    public void start_exceptionOnSecond_doNotCallErrorOnThird() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        Object message = new Object();
        NullPointerException ex = new NullPointerException();
        doThrow(ex).when(modules.get(1)).onStart(context);

        // when
        try {
            ModulesInvoker.start(context, modules);
        } catch (Exception e) {
            // expected
        }

        // then
        verify(modules.get(2), never()).onError(context, message, ex);
    }

    @Test
    public void finish_moduleThrowsDuringFinish_callOtherModules() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        ModulesInvoker sut = ModulesInvoker.start(context, modules);
        doThrow(NullPointerException.class).when(modules.get(1)).onFinished(context);

        // when
        sut.finish();

        // then
        verify(modules.get(0)).onFinished(context);
        verify(modules.get(1)).onFinished(context);
        verify(modules.get(2)).onFinished(context);
    }

    @Test
    public void error_moduleThrowsDuringError_callOtherModules() {
        // given
        CurrentExecutionContext context = mock(CurrentExecutionContext.class);
        ModulesInvoker sut = ModulesInvoker.start(context, modules);
        NullPointerException ex = new NullPointerException();
        Object message = new Object();
        doThrow(ex).when(modules.get(1)).onError(context, message, ex);

        // when
        sut.error(message, ex);

        // then
        verify(modules.get(0)).onError(context, message, ex);
        verify(modules.get(1)).onError(context, message, ex);
        verify(modules.get(2)).onError(context, message, ex);
    }
}