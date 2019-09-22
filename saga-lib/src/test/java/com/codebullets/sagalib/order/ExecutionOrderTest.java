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

package com.codebullets.sagalib.order;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.NextSagaToHandle;
import com.codebullets.sagalib.startup.StreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.timeout.InMemoryTimeoutManager;
import com.codebullets.sagalib.timeout.SystemClock;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

class ExecutionOrderTest {
    private MessageStream messageStream;
    private List<Class<?>> calledSagas;

    @BeforeEach
    void initTest() {
        calledSagas = new ArrayList<>();
    }

    @Test
    void twoStartingSaga_definedOrder_executedInOrder() {
        // given
        MessageStream stream = initWithExecutionOrder(StartingHandler2.class, StartingHandler.class);

        // when
        stream.add(new OrderedEvent("request1"));

        // then
        assertThat("Expected starting handler 2 to be executed first.", calledSagas.get(0), equalTo(StartingHandler2.class));
    }

    @Test
    void twoStartingSaga_definedReverseOrder_executedInOrder() {
        // given
        MessageStream stream = initWithExecutionOrder(StartingHandler.class, StartingHandler2.class);

        // when
        stream.add(new OrderedEvent("request1"));

        // then
        assertThat("Expected starting handler to be executed first.", calledSagas.get(0), equalTo(StartingHandler.class));
    }

    @Test
    void startAndContinueSaga_handlerFirst_executedInOrder() {
        // given
        MessageStream stream = initWithExecutionOrder(SagaWithState.class);
        stream.add(new StartingEvent("request1"));
        calledSagas.clear();

        // when
        stream.add(new OrderedEvent("request1"));

        // then
        assertThat("Expected handler 2 to be executed first.", calledSagas.get(0), equalTo(SagaWithState.class));
    }

    @SafeVarargs
    private final MessageStream initWithExecutionOrder(final Class<? extends Saga>... preferredOrder) {
        TimeoutManager timeoutManager = new InMemoryTimeoutManager(mock(ScheduledExecutorService.class), new SystemClock());

        StreamBuilder streamBuilder = EventStreamBuilder.configure()
                .usingScanner(new LocalTestScanner())
                .usingTimeoutManager(timeoutManager)
                .usingExecutor(MoreExecutors.directExecutor())
                .usingSagaProviderFactory(new InstanceCreators())
                .callingInterceptor(new SagaExecutionRecorder(calledSagas));

        if (preferredOrder.length > 0) {
            NextSagaToHandle nextSagaToHandle = streamBuilder.defineHandlerExecutionOrder().firstExecute(preferredOrder[0]);
            for (int i = 1; i < preferredOrder.length; ++i) {
                nextSagaToHandle.then(preferredOrder[i]);
            }
        }

        return streamBuilder.build();
    }

    private static class LocalTestScanner implements TypeScanner {
        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            Collection<Class<? extends Saga>> sagas = new ArrayList<>();
            sagas.add(SagaWithState.class);
            sagas.add(StartingHandler.class);
            sagas.add(StartingHandler2.class);

            return sagas;
        }
    }

    private static class SagaExecutionRecorder implements SagaLifetimeInterceptor {
        private final Collection<Class<?>> calledSagas;

        private SagaExecutionRecorder(final Collection<Class<?>> calledSagas) {
            this.calledSagas = calledSagas;
        }

        @Override
        public void onStarting(final Saga<?> saga, final ExecutionContext context, final Object message) {
        }

        @Override
        public void onHandlerExecuting(final Saga<?> saga, final ExecutionContext context, final Object message) {
            calledSagas.add(saga.getClass());
        }

        @Override
        public void onHandlerExecuted(final Saga<?> saga, final ExecutionContext context, final Object message) {
        }

        @Override
        public void onFinished(final Saga<?> saga, final ExecutionContext context) {
        }
    }
}
