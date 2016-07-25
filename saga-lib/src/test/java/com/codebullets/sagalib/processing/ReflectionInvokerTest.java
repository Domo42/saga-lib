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

import com.codebullets.sagalib.FinishMessage;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.TestSaga;
import com.codebullets.sagalib.startup.AnnotationSagaAnalyzer;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.TypeScanner;
import org.junit.Before;
import org.junit.Test;

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
public class ReflectionInvokerTest {
    private ReflectionInvoker sut;

    @Before
    public void init() {
        TypeScanner scanner = new TypeScanner() {
                @Override
                public Collection<Class<? extends Saga>> scanForSagas() {
                    Collection<Class<? extends Saga>> list = new ArrayList<>();
                    list.add(TestSaga.class);
                    return list;
                }
            };

        SagaAnalyzer analyzer = new AnnotationSagaAnalyzer(scanner);
        sut = new ReflectionInvoker(analyzer);
    }

    /**
     * Given => The saga start message.
     * When  => invoke is called.
     * Then  => Calls method marked with saga start.
     */
    @Test
    public void invoke_startupMessage_invokesSagaStartupMessage() throws InvocationTargetException, IllegalAccessException {
        // given
        String startMessage = "any string";
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        sut.invoke(saga, startMessage);

        // then
        assertThat("Expected saga start method to be called.", saga.startupCalled(), equalTo(true));
    }

    /**
     * Given => The saga handler message.
     * When  => invoke is called.
     * Then  => Calls method marked as event handler.
     */
    @Test
    public void invoke_handlerMessage_invokesHandlerMessage() throws InvocationTargetException, IllegalAccessException {
        // given
        FinishMessage handlerMessage = new FinishMessage();
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        sut.invoke(saga, handlerMessage);

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
    public void invoke_anyMessage_handleMethodNotFound_doesNotThrow() throws InvocationTargetException, IllegalAccessException {
        // given
        TestSaga saga = new TestSaga();
        saga.createNewState();

        // when
        catchException(() -> sut.invoke(saga, 42));

        // then
        assertThat("Expected no exception to be thrown.", caughtException(), is(nullValue()));
    }
}