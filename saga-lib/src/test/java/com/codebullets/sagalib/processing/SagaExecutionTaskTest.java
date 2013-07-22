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
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SagaExecutionTask} class. Note that most of the
 * tests involving this class are not here but are integration tests.
 */
public class SagaExecutionTaskTest {
    private SagaExecutionTask sut;
    private TimeoutManager timeoutManager;
    private StateStorage storage;
    private Saga saga;
    private SagaInstanceDescription sagaInstanceDescription;
    private SagaState state;

    @Before
    public void init() {
        saga = mock(Saga.class);
        state = mock(SagaState.class);
        timeoutManager = mock(TimeoutManager.class);
        storage = mock(StateStorage.class);
        SagaFactory sagaFactory = mock(SagaFactory.class);
        HandlerInvoker invoker = mock(HandlerInvoker.class);
        sagaInstanceDescription = mock(SagaInstanceDescription.class);

        Object theMessage = new Object();

        when(saga.state()).thenReturn(state);
        when(sagaInstanceDescription.getSaga()).thenReturn(saga);
        when(sagaFactory.create(theMessage)).thenReturn(Lists.newArrayList(sagaInstanceDescription));

        sut = new SagaExecutionTask(sagaFactory, invoker, storage, timeoutManager, theMessage);
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
        when(sagaInstanceDescription.isStarting()).thenReturn(true);

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
        when(sagaInstanceDescription.isStarting()).thenReturn(true);

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
        when(sagaInstanceDescription.isStarting()).thenReturn(false);

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
        when(sagaInstanceDescription.isStarting()).thenReturn(false);

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
        when(sagaInstanceDescription.isStarting()).thenReturn(true);

        // when
        sut.run();

        // then
        verify(timeoutManager, never()).cancelTimeouts(any(String.class));
    }
}