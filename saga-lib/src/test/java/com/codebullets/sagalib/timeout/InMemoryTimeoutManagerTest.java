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

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.HeaderName;
import com.codebullets.sagalib.context.SagaExecutionContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link InMemoryTimeoutManager} class.
 */
public class InMemoryTimeoutManagerTest {
    private static final HeaderName<String> DEFAULT_HEADER = HeaderName.forName("defaultHeaderName");
    private static final String DEFAULT_HEADER_VALUE = "ContextHeaderValue";
    private InMemoryTimeoutManager sut;
    private ScheduledExecutorService executor;
    private Clock clock;

    @Before
    public void init() {
        executor = mock(ScheduledExecutorService.class);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(mock(ScheduledFuture.class));

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
        sut.requestTimeout(mockContext(), "anySagaId", timeoutInSec, TimeUnit.SECONDS, "anyName", null);

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
        Timeout expected = Timeout.create(UUIDTimeoutId.generateNewId(), "theSagaId", "theTimeoutName", expectedTimeoutIn, expectedData);

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
     * Given => Callback handler has been added.
     * When  => Scheduled timer is triggered.
     * Then  => callback handler method is triggered.
     */
    @Test
    public void timeoutTriggered_contextCallbackAdded_callbackTriggered() {
        // given
        int delayInSec = 5;
        Object expectedData = new Object();
        Date expectedTimeoutIn = new Date(clock.now().getTime() + TimeUnit.SECONDS.toMillis(delayInSec));
        TimeoutExpirationCallback expiredCallback = mock(TimeoutExpirationCallback.class);
        sut.addExpiredCallback(expiredCallback);
        Timeout expected = Timeout.create(UUIDTimeoutId.generateNewId(), "theSagaId", "theTimeoutName", expectedTimeoutIn, expectedData);

        // when
        requestAndTriggerTimeout(expected.getSagaId(), expected.getName(), delayInSec, TimeUnit.SECONDS, expectedData);

        // then
        verify(expiredCallback).expired(isA(Timeout.class), isA(TimeoutExpirationContext.class));
    }

    /**
     * Given => Callback handler has been added.
     * When  => Scheduled timer is triggered.
     * Then  => callback handler method is triggered.
     */
    @Test
    public void timeoutTriggered_headerValue_headerValuePartOfCallback() {
        // given
        TimeoutExpirationCallback expiredCallback = mock(TimeoutExpirationCallback.class);
        sut.addExpiredCallback(expiredCallback);

        // when
        requestAndTriggerTimeout();

        // then
        ArgumentCaptor<TimeoutExpirationContext> captor = ArgumentCaptor.forClass(TimeoutExpirationContext.class);
        verify(expiredCallback).expired(isA(Timeout.class), captor.capture());

        Map<HeaderName<?>, Object> headers = captor.getValue().getOriginalHeaders();
        assertThat("Expected header value in callback param.", headers.get(DEFAULT_HEADER), equalTo(DEFAULT_HEADER_VALUE));
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

    /**
     * <pre>
     * Given => Timeout is added
     * When  => timeout is canceled afterwards
     * Then  => timeout is removed from schedule
     * </pre>
     */
    @Test
    public void timeoutCanceled_timeoutHasBeenAdded_timeoutRemovedFromSchedule() {
        // given
        ScheduledFuture future = mock(ScheduledFuture.class);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(future);
        TimeoutId timeoutId = sut.requestTimeout(mockContext(), "", 1, TimeUnit.DAYS, null, null);

        // when
        sut.cancelTimeout(timeoutId);

        // then
        verify(future).cancel(false);
    }

    /**
     * <pre>
     * Given => Multiple timeouts are added
     * When  => timeout is canceled afterwards
     * Then  => does not throw
     * </pre>
     */
    @Test
    public void timeoutCanceled_multipleTimeoutAdded_doesNotThrowConcurrencyException() {
        // given
        ScheduledFuture future = mock(ScheduledFuture.class);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(future);
        TimeoutId timeoutId = sut.requestTimeout(mockContext(), "saga1", 1, TimeUnit.DAYS, null, null);
        sut.requestTimeout(mockContext(), "saga2", 2, TimeUnit.DAYS, null, null);

        // when
        sut.cancelTimeout(timeoutId);

        // then
    }

    /**
     * <pre>
     * Given => Multiple timeout are added
     * When  => timeout is canceled by saga id
     * Then  => timeout is removed from schedule
     * </pre>
     */
    @Test
    public void cancelTimeouts_multipleTimeoutHaveBeenAdded_timeoutRemovedFromSchedule() {
        // given
        String sagaId = "sagaId_" + RandomStringUtils.randomAlphanumeric(5);
        ScheduledFuture future = mock(ScheduledFuture.class);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(future);
        sut.requestTimeout(mockContext(), sagaId, 1, TimeUnit.DAYS, null, null);

        // when
        sut.cancelTimeouts(sagaId);

        // then
        verify(future).cancel(false);
    }

    /**
     * <pre>
     * Given => Multiple timeout are added
     * When  => timeout is canceled by saga id
     * Then  => does not throw a concurrency exception
     * </pre>
     */
    @Test
    public void cancelTimeouts_multipleTimeoutHaveBeenAdded_noConcurrentViolationException() {
        // given
        String sagaId = "sagaId_" + RandomStringUtils.randomAlphanumeric(5);
        ScheduledFuture future = mock(ScheduledFuture.class);
        when(executor.schedule(any(Runnable.class), anyLong(), any(TimeUnit.class))).thenReturn(future);
        sut.requestTimeout(mockContext(), sagaId, 1, TimeUnit.DAYS, null, null);
        sut.requestTimeout(mockContext(), sagaId, 2, TimeUnit.DAYS, null, null);

        // when
        sut.cancelTimeouts(sagaId);

        // then
    }

    @Test
    public void close_always_shutdownExecutorService() {
        // when
        sut.close();

        // then
        verify(executor).shutdown();
    }

    private ExecutionContext mockContext() {
        SagaExecutionContext context = new SagaExecutionContext();
        context.setHeaderValue(DEFAULT_HEADER, DEFAULT_HEADER_VALUE);
        return context;
    }

    private void requestAndTriggerTimeout() {
        requestAndTriggerTimeout(
                RandomStringUtils.randomAlphanumeric(10),
                RandomStringUtils.randomAlphanumeric(10),
                10,
                TimeUnit.HOURS,
                null);
    }

    private void requestAndTriggerTimeout(
            final String sagaId,
            final String name,
            final long delay,
            final TimeUnit unit,
            final Object data) {
        sut.requestTimeout(mockContext(), sagaId, delay, unit, name, data);
        ArgumentCaptor<SagaTimeoutTask> captor = ArgumentCaptor.forClass(SagaTimeoutTask.class);
        verify(executor).schedule(captor.capture(), eq(delay), eq(unit));

        // move time forward by the expected delay
        Date oldDate = clock.now();
        when(clock.now()).thenReturn(new Date(oldDate.getTime() + unit.toMillis(delay)));

        SagaTimeoutTask timeoutTask = captor.getValue();
        timeoutTask.run();
    }
}