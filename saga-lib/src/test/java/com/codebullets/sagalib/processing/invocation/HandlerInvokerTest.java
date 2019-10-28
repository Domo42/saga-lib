/*
 * Copyright 2018 Stefan Domnanovits
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

import com.codebullets.sagalib.Saga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HandlerInvokerTest {
    private HandlerInvoker sut;
    private Saga providedSaga;
    private Object providedMessage;

    @BeforeEach
    void setUpTest() {
        sut = new TestInvoker();
    }

    @Test
    void invokeContext_contextData_calledSagaObjectOverload() throws InvocationTargetException, IllegalAccessException {
        // given
        InvocationContext context = mockContext();

        // when
        sut.invoke(context);

        // then
        assertThat("Expected saga to match context saga.", providedSaga, equalTo(context.saga()));
        assertThat("Expected message to match context message.", providedMessage, equalTo(context.message()));
    }

    private InvocationContext mockContext() {
        InvocationContext context = mock(InvocationContext.class);

        when(context.saga()).thenReturn(mock(Saga.class));
        when(context.message()).thenReturn(new Object());

        return context;
    }

    private class TestInvoker implements HandlerInvoker {
        @Override
        public void invoke(final Saga saga, final Object message) {
            providedSaga = saga;
            providedMessage = message;
        }
    }
}