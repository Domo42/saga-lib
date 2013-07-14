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
package com.codebullets.sagalib.timeout;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InMemoryTimeoutManager} class.
 */
public class InMemoryTimeoutManagerTest {
    private InMemoryTimeoutManager sut;
    private ScheduledExecutorService executor;
    private Clock clock;

    @Before
    public void init() {
        executor = mock(ScheduledExecutorService.class);

        clock = mock(Clock.class);
        when(clock.now()).thenReturn(new Date());

        sut = new InMemoryTimeoutManager(executor, clock);
    }

    /**
     * Given => The timeout message.
     * When  => requestTimeout is called.
     * Then  => Add timed task to scheduler.
     */
    @Test
    public void requestTimeout_TimeoutMessage_schedulesTimeoutTask() {
        // given
        long timeoutInSec = 5;

        // when
        sut.requestTimeout("anySagaId", timeoutInSec, TimeUnit.SECONDS, "anyName", null);

        // then
        ArgumentCaptor<SagaTimeoutTask> captor = ArgumentCaptor.forClass(SagaTimeoutTask.class);
        verify(executor).schedule(captor.capture(), eq(timeoutInSec), eq(TimeUnit.SECONDS));

        assertThat("Scheduled task not of expected type.", captor.getValue(), instanceOf(SagaTimeoutTask.class));
    }

    /**
     * Given => Callback handler has been added.
     * When  => Scheduled timer is triggered.
     * Then  => callback handler method is triggered.
     */
    @Test
    public void timeoutTriggered_callbackAdded_callbackTriggered() {
        // given
        int delayInSec = 5;
        Object expectedData = new Object();
        Date expectedTimeoutIn = new Date(clock.now().getTime() + TimeUnit.SECONDS.toMillis(delayInSec));
        TimeoutExpired expiredCallback = mock(TimeoutExpired.class);
        sut.addExpiredCallback(expiredCallback);
        Timeout expected = Timeout.create("theSagaId", "theTimeoutName", expectedTimeoutIn, expectedData);

        // when
        requestAndTriggerTimeout(expected.getSagaId(), expected.getName(), delayInSec, TimeUnit.SECONDS, expectedData);

        // then
        ArgumentCaptor<Timeout> captor = ArgumentCaptor.forClass(Timeout.class);
        verify(expiredCallback).expired(captor.capture());

        assertThat("Saga id does not match.", captor.getValue().getSagaId(), equalTo(expected.getSagaId()));
        assertThat("Timeout name does not match.", captor.getValue().getName(), equalTo(expected.getName()));
        assertThat("Expiration time stamp does not match.", captor.getValue().getExpiredAt(), equalTo(expected.getExpiredAt()));
        assertThat("Data object does not match.", captor.getValue().getData(), sameInstance(expectedData));
    }

    /**
     * Given => Multiple timeouts registered with timeout manager.
     * When  => Scheduled timer is triggered.
     * Then  => All registered callbacks are informed.
     */
    @Test
    public void timeoutTriggered_multipleCallbacksAdded_allRegisteredCallbacksAreTriggered() {
        // given
        TimeoutExpired callback1 = mock(TimeoutExpired.class);
        TimeoutExpired callback2 = mock(TimeoutExpired.class);
        sut.addExpiredCallback(callback1);
        sut.addExpiredCallback(callback2);

        // when
        requestAndTriggerTimeout();

        // then
        verify(callback1).expired(isA(Timeout.class));
        verify(callback2).expired(isA(Timeout.class));
    }

    private void requestAndTriggerTimeout() {
        requestAndTriggerTimeout(
                RandomStringUtils.randomAlphanumeric(10),
                RandomStringUtils.randomAlphanumeric(10),
                10,
                TimeUnit.HOURS,
                null);
    }

    private void requestAndTriggerTimeout(final String sagaId, final String name, final long delay, final TimeUnit unit, final Object data) {
        sut.requestTimeout(sagaId, delay, unit, name, data);
        ArgumentCaptor<SagaTimeoutTask> captor = ArgumentCaptor.forClass(SagaTimeoutTask.class);
        verify(executor).schedule(captor.capture(), eq(delay), eq(unit));

        // move time forward by the expected delay
        Date oldDate = clock.now();
        when(clock.now()).thenReturn(new Date(oldDate.getTime() + unit.toMillis(delay)));

        SagaTimeoutTask timeoutTask = captor.getValue();
        timeoutTask.run();
    }
}