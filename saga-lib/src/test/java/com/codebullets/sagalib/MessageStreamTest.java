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
package com.codebullets.sagalib;

import com.codebullets.sagalib.context.SagaExecutionContext;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.storage.MemoryStorage;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.SagaTimeoutTask;
import com.codebullets.sagalib.timeout.SystemClock;
import com.codebullets.sagalib.timeout.Timeout;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.codebullets.sagalib.timeout.UUIDTimeoutId;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration Tests for the saga lib message stream interface.
 */
public class MessageStreamTest {
    private MessageStream sut;
    private StateStorage storage;
    private Set<Number> numbers;
    private Set<String> calledSagas;
    ScheduledExecutorService scheduler;
    ScheduledFuture timeout;
    private SagaInterceptor interceptor = new SagaInterceptor();
    private Map<HeaderName<?>, Object> foundContextHeaders = new HashMap<>();

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        storage = new MemoryStorage();
        scheduler = mock(ScheduledExecutorService.class);
        timeout = mock(ScheduledFuture.class);
        numbers = new TreeSet<>();
        calledSagas = new TreeSet<>();
        TimeoutManager timeoutManager = new InMemoryTimeoutManager(scheduler, new SystemClock());

        when(scheduler.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(timeout);

        sut = EventStreamBuilder.configure()
                .usingStorage(storage)
                .usingScanner(new LocalScanner())
                .usingSagaProviderFactory(new TestSagaProviderFactory(timeoutManager, numbers, calledSagas))
                .usingTimeoutManager(timeoutManager)
                .callingInterceptor(interceptor)
                .usingExecutor(MoreExecutors.directExecutor())
                .defineHandlerExecutionOrder()
                    .firstExecute(NumberSaga.class).builder()
                .build();
    }

    /**
     * Given => string message as input.
     * When  => Message is handled by stream.
     * Then  => Saga is started.
     */
    @Test
    public void handle_stringMessage_messageIsHandledBySaga() throws InvocationTargetException, IllegalAccessException {
        // given
        String message = "myTestMessage_" + RandomStringUtils.randomAlphanumeric(10);

        // when
        sut.handle(message);

        // then
        Collection<SagaState> sagaState = convertToCollection(storage.load(TestSaga.class.getName(), TestSaga.INSTANCE_KEY));
        assertThat("Expected entry presenting the started saga state.", sagaState, hasSize(1));
    }

    /**
     * Given => First string and then integer message.
     * When  => Messages are handled by the stream.
     * Then  => Saga is completed and therefore no longer in storage.
     */
    @Test
    public void handle_stringAndIntegerMessage_sagaFinishedAndNoLongerInStateStorage() throws InvocationTargetException, IllegalAccessException {
        // given
        String msg1 = "myTestMessage_" + RandomStringUtils.randomAlphanumeric(10);
        FinishMessage msg2 = new FinishMessage(TestSaga.INSTANCE_KEY);

        // when
        sut.handle(msg1);
        sut.handle(msg2);

        // then
        Collection<SagaState> sagaState = convertToCollection(storage.load(TestSaga.class.getName(), TestSaga.INSTANCE_KEY));
        assertThat("Expected no longer an entry for the saga state.", sagaState, hasSize(0));
    }

    /**
     * Given => Saga has been started.
     * When  => Timeout message is triggered.
     * Then  => Timeout handler is called.
     */
    @Test
    public void handle_sagaHasBeenStartedTimeoutReceived_timeoutHandlerIsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        String msg = "myTestMessage_" + RandomStringUtils.randomAlphanumeric(10);
        sut.handle(msg);

        // when
        triggerTimeout();

        // then
        Collection<SagaState> sagaStates = convertToCollection(storage.load(TestSaga.class.getName(), TestSaga.INSTANCE_KEY));
        TestSagaState knownState = (TestSagaState) sagaStates.iterator().next();

        assertThat("Expected timeout to be called.", knownState.isTimeoutHandled(), equalTo(true));
    }

    /**
     * Given => Saga is started and completed.
     * When  => Last message is handled.
     * Then  => Timeout is canceled.
     */
    @Test
    public void handle_sagaIsCompleted_runningTimeoutIsCanceled() throws InvocationTargetException, IllegalAccessException {
        // given
        String startMsg = "startMsg_" + RandomStringUtils.randomAlphanumeric(10);
        FinishMessage msg2 = new FinishMessage(TestSaga.INSTANCE_KEY);
        sut.handle(startMsg);

        // when
        sut.handle(msg2);

        // then
        verify(timeout).cancel(eq(false));
    }

    /**
     * Given => Saga is started but not completed.
     * When  => Message is handled.
     * Then  => Timeout is not canceled.
     */
    @Test
    public void handle_sagaNotCompleted_doNotCancelTimeouts() throws InvocationTargetException, IllegalAccessException {
        // given
        String startMsg = "startMsg_" + RandomStringUtils.randomAlphanumeric(10);

        // when
        sut.handle(startMsg);

        // then
        verify(timeout, never()).cancel(anyBoolean());
    }

    /**
     * <pre>
     * Given => Message of type integer.
     * When  => Message is handled.
     * Then  => Base class handler has been called.
     * </pre>
     */
    @Test
    public void handle_integerMessage_callHandlerOfBaseClass() throws InvocationTargetException, IllegalAccessException {
        // given
        Integer message = new Random().nextInt();

        // when
        sut.handle(message);

        // then
        assertThat("Expected number from message to be stored (proof handler has been called.)", numbers, hasItem(message));
    }

    /**
     * <pre>
     * Given => Message of type integer. Base class handler (Number) stops message dispatching.
     * When  => Message is handled.
     * Then  => Concrete handler saga for integer is never called.
     * </pre>
     */
    @Test
    public void handle_integerMessage_integerSagaNeverInvoked() throws InvocationTargetException, IllegalAccessException {
        // given
        Integer message = new Random().nextInt();

        // when
        sut.handle(message);

        // then
        assertThat("Number saga is never called.", calledSagas, not(hasItem(IntegerSaga.class.getName())));
    }

    /**
     * <pre>
     * Given => Message created where no saga handler exists.
     * When  => Message is handled
     * Then  => Dead message handler is created.
     * </pre>
     */
    @Test
    public void handle_messageWithNoHandler_deadMessageSagaIsStarted() throws InvocationTargetException, IllegalAccessException {
        // given
        Object message = new Object() {};

        // when
        sut.handle(message);

        // then
        Optional<Saga> deadMsgSaga = Iterables.tryFind(interceptor.getStartedSagas(), Predicates.instanceOf(DeadMessageSaga.class));
        assertThat("Expected DeadMessageSaga in list of started sagas.", deadMsgSaga.isPresent(), equalTo(true));
    }

    /**
     * <pre>
     * Given => Timeout message where handler exists but no saga state
     * When  => Message is handled
     * Then  => Does not result in an exception
     * </pre>
     */
    @Test
    public void handle_timeoutWithNoInstance_doesNotThrow() throws InvocationTargetException, IllegalAccessException {
        // given
        Timeout timeout = Timeout.create(UUIDTimeoutId.generateNewId(), "theSagaId", null, new Date());

        // when
        sut.handle(timeout);

        // then
        assertThat("Expected this line to be reached.", true);
    }

    @Test
    public void handleMessage_customHeaders_contextContainsHeaders() throws InvocationTargetException, IllegalAccessException {
        // given
        HeaderName<String> headerName = HeaderName.forName("customHeader");
        String expectedHeaderValue = "expectedHeaderValue";
        Map<HeaderName<?>, Object> headers = ImmutableMap.of(headerName, expectedHeaderValue);

        // given
        sut.handleMessage("anyMessage", headers);

        // then
        Object actualHeaderValue = interceptor.getFoundExecutionHeaders().get(headerName);
        assertThat("Expected the supplied header value to be part of context.", actualHeaderValue, equalTo(expectedHeaderValue));
    }

    @Test
    public void handleMessage_parentContextWithHeaders_contextContainsMergedHeaders() throws InvocationTargetException, IllegalAccessException {
        // given
        HeaderName<String> header1 = HeaderName.forName("header1");
        HeaderName<String> header2 = HeaderName.forName("header2");
        String headerVal1 = "expectedHeaderValue";
        String headerVal2 = "parentContextVal";
        Map<HeaderName<?>, Object> headers = ImmutableMap.of(header1, headerVal1);
        ExecutionContext parentContext = stubContext(header2, headerVal2);

        // given
        sut.handleMessage("anyMessage", headers, parentContext);

        // then
        Object actualVal1 = interceptor.getFoundExecutionHeaders().get(header1);
        Object actualVal2 = interceptor.getFoundExecutionHeaders().get(header2);
        assertThat("Expected the supplied header value to be part of context.", actualVal1, equalTo(headerVal1));
        assertThat("Expected the supplied header value to be part of context.", actualVal2, equalTo(headerVal2));
    }

    private <T> ExecutionContext stubContext(final HeaderName<T> headerName, final T headerValue) {
        SagaExecutionContext newContext = new SagaExecutionContext();
        newContext.setHeaderValue(headerName, headerValue);

        return newContext;
    }

    private <T> Collection<T> convertToCollection(Collection <? extends T> source) {
        Collection<T> newCollection = new ArrayList<>(source.size());
        for (T entry : source) {
            newCollection.add(entry);
        }

        return newCollection;
    }

    private void triggerTimeout() {
        ArgumentCaptor<SagaTimeoutTask> captor = ArgumentCaptor.forClass(SagaTimeoutTask.class);
        verify(scheduler).schedule(captor.capture(), anyLong(), any(TimeUnit.class));

        // move time forward by the expected delay
        SagaTimeoutTask timeoutTask = captor.getValue();
        timeoutTask.run();
    }

    private static class LocalScanner implements TypeScanner {

        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            Collection<Class<? extends Saga>> sagas = new ArrayList<>();
            sagas.add(TestSaga.class);
            sagas.add(IntegerSaga.class);
            sagas.add(NumberSaga.class);
            sagas.add(DeadMessageSaga.class);

            return sagas;
        }
    }

    private static class TestSagaProviderFactory implements SagaProviderFactory {
        private final TimeoutManager timeoutManager;
        private final Set<Number> numbers;
        private final Set<String> calledSagas;

        /**
         * Generates a new instance of MessageStreamTest$TestSagaProviderFactory.
         */
        public TestSagaProviderFactory(TimeoutManager timeoutManager, Set<Number> numbers, final Set<String> calledSagas) {
            this.timeoutManager = timeoutManager;
            this.numbers = numbers;
            this.calledSagas = calledSagas;
        }

        @Override
        public Provider<? extends Saga> createProvider(final Class sagaClass) {
            Provider<? extends Saga> provider = null;

            if (sagaClass.equals(TestSaga.class)) {
                provider = new Provider<TestSaga>() {
                    @Override
                    public TestSaga get() {
                        return new TestSaga(timeoutManager);
                    }
                };
            } else if (sagaClass.equals(NumberSaga.class)) {
                provider = new Provider<NumberSaga>() {
                    public NumberSaga get() {
                        return new NumberSaga(numbers);
                    }
                };
            } else if (sagaClass.equals(IntegerSaga.class)) {
                provider = new Provider<Saga>() {
                        @Override
                        public Saga get() {
                            return new IntegerSaga(calledSagas);
                        }
                    };
            } else if (sagaClass.equals(DeadMessageSaga.class)) {
                provider = new Provider<Saga>() {
                        @Override
                        public Saga get() {
                            return new DeadMessageSaga();
                        }
                    };
            }

            return provider;
        }
    }

    private static class SagaInterceptor implements SagaLifetimeInterceptor {
        private Collection<Saga> startedSagas = new ArrayList<>();
        private Map<HeaderName<?>, Object> foundExecutionHeaders;

        Collection<Saga> getStartedSagas() {
            return startedSagas;
        }

        Map<HeaderName<?>, Object> getFoundExecutionHeaders() {
            return foundExecutionHeaders;
        }

        @Override
        public void onStarting(final Saga<?> saga, final ExecutionContext context, final Object message) {
            startedSagas.add(saga);
        }

        @Override
        public void onHandlerExecuting(final Saga<?> saga, final ExecutionContext context, final Object message) {
            foundExecutionHeaders = Headers.copyFromStream(context.getAllHeaders());
        }

        @Override
        public void onHandlerExecuted(final Saga<?> saga, final ExecutionContext context, final Object message) {
        }

        @Override
        public void onFinished(final Saga<?> saga, final ExecutionContext context) {
        }
    }
}