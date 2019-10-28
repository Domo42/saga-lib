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
package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.FinishMessage;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.TestSaga;
import com.codebullets.sagalib.startup.AnnotationSagaAnalyzer;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.TypeScanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Tests for {@link ReflectionInvoker} class.
 */
class ReflectionInvokerTest {
    private ReflectionInvoker sut;

    @BeforeEach
    void init() {
        sut = sutWithScannedSaga(TestSaga.class);
    }

    /**
     * Given => The saga start message.
     * When  => invoke is called.
     * Then  => Calls method marked with saga start.
     */
    @Test
    void invoke_startupMessage_invokesSagaStartupMessage() throws InvocationTargetException, IllegalAccessException {
        // given
        String startMessage = "any string";
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        sut.invoke(createContext(saga, startMessage));

        // then
        assertThat("Expected saga start method to be called.", saga.startupCalled(), equalTo(true));
    }

    /**
     * Given => The saga handler message.
     * When  => invoke is called.
     * Then  => Calls method marked as event handler.
     */
    @Test
    void invoke_handlerMessage_invokesHandlerMessage() throws InvocationTargetException, IllegalAccessException {
        // given
        FinishMessage handlerMessage = new FinishMessage();
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        sut.invoke(createContext(saga, handlerMessage, InvocationHandlerType.CONTINUE));

        // then
        assertThat("Expected saga handler method to be called.", saga.handlerCalled(), equalTo(true));
    }

    /**
     * <pre>
     * Given => no handler method for message available
     * When  => invoke is called
     * Then  => does not throw
     * </pre>
     */
    @Test
    void invoke_anyMessage_handleMethodNotFound_doesNotThrow() {
        // given
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        catchException(() -> sut.invoke(createContext(saga, 42)));

        // then
        assertThat("Expected no exception to be thrown.", caughtException(), is(nullValue()));
    }

    @Test
    void invoke_startingContext_callsStartHandler() throws InvocationTargetException, IllegalAccessException {
        // given
        sut = sutWithSameMessageHandler();
        SameMessageHandlers saga = new SameMessageHandlers();
        saga.createNewState();

        // when
        sut.invoke(createContext(saga, "message", InvocationHandlerType.START));

        // then
        assertThat("Expected the start handler to be executed.", saga.isStartHandlerCalled(), is(true));
    }

    @Test
    void invoke_continueContext_callsContinueHandler() throws InvocationTargetException, IllegalAccessException {
        // given
        sut = sutWithSameMessageHandler();
        SameMessageHandlers saga = new SameMessageHandlers();
        saga.createNewState();

        // when
        sut.invoke(createContext(saga, "message", InvocationHandlerType.CONTINUE));

        // then
        assertThat("Expected the continue handler to be executed.", saga.isContinueHandlerCalled(), is(true));
    }

    private ReflectionInvoker sutWithSameMessageHandler() {
        return sutWithScannedSaga(SameMessageHandlers.class);
    }

    private ReflectionInvoker sutWithScannedSaga(final Class<? extends Saga> sagaClass) {
        TypeScanner scanner = () -> {
            Collection<Class<? extends Saga>> list = new ArrayList<>();
            list.add(sagaClass);
            return list;
        };

        SagaAnalyzer analyzer = new AnnotationSagaAnalyzer(scanner);
        return new ReflectionInvoker(analyzer);
    }

    private InvocationContext createContext(final Saga saga, final Object message) {
        return createContext(saga, message, InvocationHandlerType.START);
    }

    private InvocationContext createContext(final Saga saga, final Object message, final InvocationHandlerType handlerType) {
        return new InvocationContext() {
            @Override
            public ExecutionContext context() {
                return null;
            }

            @Override
            public InvocationHandlerType handlerType() {
                return handlerType;
            }

            @Override
            public Saga<?> saga() {
                return saga;
            }

            @Override
            public Object message() {
                return message;
            }
        };
    }
}