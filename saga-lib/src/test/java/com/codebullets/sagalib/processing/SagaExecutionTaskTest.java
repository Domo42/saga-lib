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

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.HeaderName;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.context.NeedContext;
import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.invocation.DefaultModuleCoordinator;
import com.codebullets.sagalib.processing.invocation.HandlerInvoker;
import com.codebullets.sagalib.processing.invocation.SagaExecutionErrorsException;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for {@link SagaExecutionTask} class. Note that most of the
 * tests involving this class are not here but are integration tests.
 */
@SuppressWarnings("unchecked")
public class SagaExecutionTaskTest {
    private SagaExecutionTask sut;
    private TimeoutManager timeoutManager;
    private StateStorage storage;
    private Saga saga;
    private SagaInstanceInfo sagaInstanceInfo;
    private SagaState state;
    private CurrentExecutionContext context;
    private Object theMessage;
    private HandlerInvoker invoker;
    private InstanceResolver instanceResolver;
    private SagaModule module;
    private SagaLifetimeInterceptor interceptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        saga = mock(Saga.class, withSettings().extraInterfaces(NeedContext.class));
        state = mock(SagaState.class);
        timeoutManager = mock(TimeoutManager.class);
        storage = mock(StateStorage.class);
        instanceResolver = mock(InstanceResolver.class);
        invoker = mock(HandlerInvoker.class);
        sagaInstanceInfo = mock(SagaInstanceInfo.class);
        module = mock(SagaModule.class);
        interceptor = mock(SagaLifetimeInterceptor.class);

        theMessage = new Object();

        when(saga.state()).thenReturn(state);
        when(sagaInstanceInfo.getSaga()).thenReturn(saga);
        when(instanceResolver.resolve(isA(LookupContext.class))).thenReturn(Lists.newArrayList(sagaInstanceInfo));

        Provider<CurrentExecutionContext> contextProvider = mockExecutionContext();
        SagaEnvironment env = SagaEnvironment.create(
                timeoutManager,
                storage,
                contextProvider,
                Sets.newHashSet(module),
                Sets.newHashSet(interceptor),
                instanceResolver,
                DefaultModuleCoordinator::new);
        sut = new SagaExecutionTask(env, invoker, theMessage, new HashMap<>(), null);
    }

    /**
     * Given => Handled saga is not completed.
     * When  => Saga is invoked.
     * Then  => Saga state is saved.
     */
    @Test
    public void run_sagaNotCompleted_saveSagaState() {
        // given
        when(saga.isFinished()).thenReturn(false);

        // when
        sut.run();

        // then
        verify(storage).save(state);
    }

    /**
     * Given => Handled saga is started and finished after invocation.
     * When  => Task is executed.
     * Then  => Does not save state.
     */
    @Test
    public void run_sagaIsStartedAndFinished_doesNotSaveSate() {
        // given
        when(saga.isFinished()).thenReturn(true);
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        verify(storage, never()).save(any(SagaState.class));
    }

    /**
     * <pre>
     * Given => Handled saga is started and finished after invocation.
     * When  => Task is executed.
     * Then  => Does not delete state from storage as starting flag indicates state has never been
     *          persisted.
     * </pre>
     */
    @Test
    public void run_sagaIsStartedAndFinished_doesNotDeleteSate() {
        // given
        when(saga.isFinished()).thenReturn(true);
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        verify(storage, never()).delete(any(String.class));
    }

    /**
     * <pre>
     * Given => Saga is continued (not starting) and finished.
     * When  => Task is executed.
     * Then  => Delete saga state from storage.
     * </pre>
     */
    @Test
    public void run_sagaHasContinuedAndIsFinished_deleteSagaState() {
        // given
        final String sagaId = RandomStringUtils.randomAlphanumeric(10);
        when(state.getSagaId()).thenReturn(sagaId);
        when(saga.isFinished()).thenReturn(true);
        when(sagaInstanceInfo.isStarting()).thenReturn(false);

        // when
        sut.run();

        // then
        verify(storage).delete(sagaId);
    }

    /**
     * <pre>
     * Given => Saga is continued (not starting) and finished.
     * When  => Task is executed.
     * Then  => Delete possible open timeouts.
     * </pre>
     */
    @Test
    public void run_sagaHasContinuedAndIsFinished_cancelTimeouts() {
        // given
        final String sagaId = RandomStringUtils.randomAlphanumeric(10);
        when(state.getSagaId()).thenReturn(sagaId);
        when(saga.isFinished()).thenReturn(true);
        when(sagaInstanceInfo.isStarting()).thenReturn(false);

        // when
        sut.run();

        // then
        verify(timeoutManager).cancelTimeouts(sagaId);
    }

    /**
     * <pre>
     * Given => Saga has been started and is immediately finished.
     * When  => Task is executed.
     * Then  => Does not cancel any timeouts (Not expected as saga has just started)
     * </pre>
     */
    @Test
    public void run_sagaIsStartedAndIsFinished_doNotCancelTimeouts() {
        // given
        when(saga.isFinished()).thenReturn(true);
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        verify(timeoutManager, never()).cancelTimeouts(any(String.class));
    }

    /**
     * <pre>
     * Given => Specific message for saga handling.
     * When  => Task is executed.
     * Then  => Expected message to be set on saga context. Do this before saga is invoked
     * </pre>
     */
    @Test
    public void run_messageParam_messageIsSetOnContext() throws InvocationTargetException, IllegalAccessException {
        // given, when
        sut.run();

        // then
        InOrder inOrder = inOrder(context, invoker);

        inOrder.verify(context).setMessage(theMessage);
        inOrder.verify(invoker).invoke(saga, theMessage);
    }

    /**
     * <pre>
     * Given => specific saga being invoked
     * When  => task is executed
     * Then  => saga is set on context. has to be done before invoke
     * </pre>
     */
    @Test
    public void run_sagaToInvoke_sagaIsSetOnContext() throws InvocationTargetException, IllegalAccessException {
        // given, when
        sut.run();

        // when
        InOrder inOrder = inOrder(context, invoker);

        inOrder.verify(context).setSaga(saga);
        inOrder.verify(invoker).invoke(saga, theMessage);
    }

    /**
     * <pre>
     * Given => header values are provided.
     * When  => saga task is executed
     * Then  => saga context contains header value.
     * </pre>
     */
    @Test
    public void run_headerValuesProvided_contextContainsHeader() {
        // given
        CurrentExecutionContext context = new SagaExecutionContext();
        HeaderName<String> headerName = HeaderName.forName("headerKey");
        Object headerValue = "headerValue";
        Map<HeaderName<?>, Object> headers = new HashMap<>();
        headers.put(headerName, headerValue);
        SagaEnvironment env = SagaEnvironment.create(
                timeoutManager,
                storage,
                createContextProvider(context),
                Sets.newHashSet(module),
                Sets.newHashSet(interceptor),
                instanceResolver,
                DefaultModuleCoordinator::new);
        sut = new SagaExecutionTask(env, invoker, theMessage, headers, null);

        // when
        sut.run();

        // then
        assertThat("Expected header value to be part of context.", context.getHeaderValue(headerName).get(), equalTo(headerValue));
    }

    /**
     * <pre>
     * Given => Parent execution context is provided
     * When  => task runs
     * Then  => Parent context set as part of execution context.
     * </pre>
     */
    @Test
    public void run_parentContextProvided_contextHasParentContext() {
        // given
        CurrentExecutionContext context = new SagaExecutionContext();
        ExecutionContext parentContext = mock(ExecutionContext.class);

        SagaEnvironment env = SagaEnvironment.create(timeoutManager, storage, createContextProvider(context), Sets.newHashSet(module),
                Sets.newHashSet(interceptor), instanceResolver, DefaultModuleCoordinator::new);
        sut = new SagaExecutionTask(env, invoker, theMessage, Collections.EMPTY_MAP, parentContext);

        // when
        sut.run();

        // then
        assertThat("Expected header value to be part of context.", context.parentContext(), sameInstance(parentContext));
    }

    /**
     * <pre>
     * Given => Module is available.
     * When  => saga task is executed
     * Then  => start called on module before saga is invoked
     * </pre>
     */
    @Test
    public void run_usingModule_moduleStartedBeforeSaga() throws InvocationTargetException, IllegalAccessException {
        // given, when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, module);
        inOrder.verify(module).onStart(context);
        inOrder.verify(invoker).invoke(saga, theMessage);
    }

    /**
     * <pre>
     * Given => Module is available.
     * When  => saga task is executed
     * Then  => finished is called on module after sagas are invoked
     * </pre>
     */
    @Test
    public void run_usingModule_moduleFinishedAfterSaga() throws InvocationTargetException, IllegalAccessException {
        // given, when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, module);
        inOrder.verify(invoker).invoke(saga, theMessage);
        inOrder.verify(module).onFinished(context);
    }

    /**
     * <pre>
     * Given => invoking the saga throws an exception
     * When  => saga task is executed
     * Then  => module finished is still called
     * </pre>
     */
    @Test
    public void run_invokeThrows_moduleFinishedGetsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        doThrow(NullPointerException.class).when(invoker).invoke(saga, theMessage);

        try {
            // when
            sut.run();
        } catch (NullPointerException ex) {
            // got you
        }

        // then
        verify(module).onFinished(context);
    }

    @Test
    public void run_moduleStartThrows_moduleFinishedGetsCalled() throws Exception {
        // given
        doThrow(NullPointerException.class).when(module).onStart(context);

        try {
            // when
            sut.run();
        } catch (NullPointerException ex) {
            // got you
        }

        // then
        verify(module).onFinished(context);
    }

    @Test
    public void run_moduleStartThrows_moduleErrorGetsCalled() throws Exception {
        // given
        NullPointerException expected = new NullPointerException();
        doThrow(expected).when(module).onStart(context);

        try {
            // when
            sut.run();
        } catch (NullPointerException ex) {
            // got you
        }

        // then
        verify(module).onError(context, theMessage, expected);
    }

    /**
     * <pre>
     * Given => invoking the saga throws an exception
     * When  => saga task is executed
     * Then  => module error is called.
     * </pre>
     */
    @Test
    public void run_invokeThrows_moduleErrorGetsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        NullPointerException npe = new NullPointerException();
        doThrow(npe).when(invoker).invoke(saga, theMessage);

        try {
            // when
            sut.run();
        } catch (NullPointerException ex) {
            // got you
        }

        // then
        verify(module).onError(context, theMessage, npe);
    }
    
    /**
     * <pre>
     * Given => invoking the saga throws exception
     * When  => saga task is executed
     * Then  => exception still propagated to outside
     * </pre>
     */
    @Test
    public void run_invokeThrows_publicRunThrows() throws InvocationTargetException, IllegalAccessException {
        thrown.expect(NullPointerException.class);

        // given
        doThrow(NullPointerException.class).when(invoker).invoke(saga, theMessage);

        // when
        sut.run();
    }

    /**
     * <pre>
     * Given => Interceptor is available.
     * When  => saga task is executed
     * Then  => onStarting called on interceptor before saga is invoked
     * </pre>
     */
    @Test
    public void run_usingInterceptor_interceptorStartCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, interceptor);
        inOrder.verify(interceptor).onStarting(saga, context, theMessage);
        inOrder.verify(invoker).invoke(saga, theMessage);
    }

    /**
     * <pre>
     * Given => Interceptor is available, saga is continuing execution
     * When  => saga task is executed
     * Then  => onStarting not called on interceptor
     * </pre>
     */
    @Test
    public void run_usingInterceptor_interceptorNotCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(false);

        // when
        sut.run();

        // then
        verify(interceptor, never()).onStarting(any(Saga.class), any(ExecutionContext.class), any());
    }

    /**
     * <pre>
     * Given => Interceptor is available, saga is finished
     * When  => saga task is executed
     * Then  => onFinished called on interceptor
     * </pre>
     */
    @Test
    public void run_usingInterceptorSagaFinished_interceptorFinishedCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(true);
        when(saga.isFinished()).thenReturn(true);

        // when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, interceptor);
        inOrder.verify(invoker).invoke(saga, theMessage);
        inOrder.verify(interceptor).onFinished(saga, context);
    }

    /**
     * <pre>
     * Given => Interceptor is available, saga is not finished
     * When  => saga task is executed
     * Then  => onFinished not called on interceptor
     * </pre>
     */
    @Test
    public void run_usingInterceptorSagaNotFinished_interceptorFinishedNotCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(true);
        when(saga.isFinished()).thenReturn(false);

        // when
        sut.run();

        // then
        verify(interceptor, never()).onFinished(any(Saga.class), any(ExecutionContext.class));
    }

    /**
     * <pre>
     * Given => Interceptor is available
     * When  => saga task is executed
     * Then  => onHandlerExecuting called on interceptor
     * </pre>
     */
    @Test
    public void run_usingInterceptor_interceptorHandlerExecutingCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, interceptor);
        inOrder.verify(interceptor).onHandlerExecuting(saga, context, theMessage);
        inOrder.verify(invoker).invoke(saga, theMessage);
    }

    /**
     * <pre>
     * Given => Interceptor is available
     * When  => saga task is executed
     * Then  => onHandlerExecuted called on interceptor
     * </pre>
     */
    @Test
    public void run_usingInterceptor_interceptorHandlerExecutedCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        when(sagaInstanceInfo.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        InOrder inOrder = inOrder(invoker, interceptor);
        inOrder.verify(invoker).invoke(saga, theMessage);
        inOrder.verify(interceptor).onHandlerExecuted(saga, context, theMessage);
    }

    /**
     * <pre>
     * Given => SagaModule stops dispatching
     * When  => saga task is executed
     * Then  => invoke shall not be called
     * </pre>
     */
    @Test
    public void run_sagaModuleStopsDispatching_invokeShallNotBeCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        doAnswer(invocationOnMock -> {
            ((ExecutionContext) invocationOnMock.getArguments()[0]).stopDispatchingCurrentMessageToHandlers();
            return null;
        }).when(module).onStart(any());

        // when
        sut.run();

        // then
        verify(invoker, never()).invoke(saga, theMessage);
    }

    @Test
    public void run_invokerAndModuleFinishThrow_throwsSagaExecutionExceptions() throws InvocationTargetException, IllegalAccessException {
        // given
        doThrow(ArithmeticException.class).when(invoker).invoke(saga, theMessage);
        doThrow(NullPointerException.class).when(module).onFinished(any(ExecutionContext.class));

        // when
        catchException(() -> sut.run());

        // then
        SagaExecutionErrorsException exception = caughtException();
        assertThat("Expected two exceptions in resulting one.", exception.getExecutionErrors(), hasSize(2));
        assertThat("Expected two exceptions in resulting one.", exception.getExecutionErrors(), hasItem(instanceOf(NullPointerException.class)));
        assertThat("Expected two exceptions in resulting one.", exception.getExecutionErrors(), hasItem(instanceOf(ArithmeticException.class)));
    }

    private Provider<CurrentExecutionContext> createContextProvider(final CurrentExecutionContext context) {
        return () -> context;
    }

    private Provider<CurrentExecutionContext> mockExecutionContext() {
        context = mock(CurrentExecutionContext.class);
        Provider<CurrentExecutionContext> contextProvider = mock(Provider.class);
        when(contextProvider.get()).thenReturn(context);

        when(context.error()).thenReturn(Optional.empty());

        doAnswer(invocationOnMock -> {
            Object message = invocationOnMock.getArguments()[0];
            when(context.message()).thenReturn(message);
            return null;
        }).when(context).setMessage(any());

        doAnswer(invocationOnMock -> {
            when(context.dispatchingStopped()).thenReturn(true);
            return null;
        }).when(context).stopDispatchingCurrentMessageToHandlers();

        doAnswer(invocationOnMock -> {
            Exception error = invocationOnMock.getArgument(0);
            when(context.error()).thenReturn(Optional.of(error));
            return null;
        }).when(context).setError(any(Exception.class));

        return contextProvider;
    }
}