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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reports timeouts placed on in memory state and timers.
 */
public class InMemoryTimeoutManager implements TimeoutManager {
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryTimeoutManager.class);
    private static final int TIMER_THREAD_POOL_SIZE = 50;
    private final Object sync = new Object();

    private final Collection<TimeoutExpired> callbacks = Collections.synchronizedCollection(new ArrayList<TimeoutExpired>());
    private final Table<TimeoutId, String, ScheduledFuture> openTimeouts = HashBasedTable.create();

    private final ScheduledExecutorService scheduledService;
    private final Clock clock;

    /**
     * Generates a new instance of InMemoryTimeoutManager.
     */
    public InMemoryTimeoutManager() {
        this.scheduledService = Executors.newScheduledThreadPool(TIMER_THREAD_POOL_SIZE);
        this.clock = new SystemClock();
    }

    /**
     * Generates a new instance of InMemoryTimeoutManager.
     */
    public InMemoryTimeoutManager(final ScheduledExecutorService scheduledService, final Clock clock) {
        this.scheduledService = scheduledService;
        this.clock = clock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addExpiredCallback(final TimeoutExpired callback) {
        checkNotNull(callback, "Expired callback not allowed to be null.");

        callbacks.add(callback);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeoutId requestTimeout(final ExecutionContext context, final String sagaId, final long delay, final TimeUnit timeUnit, @Nullable final String name,
                               @Nullable final Object data) {
        checkNotNull(sagaId, "SagaId not allowed to be null.");

        UUIDTimeoutId id = UUIDTimeoutId.generateNewId();

        SagaTimeoutTask timeoutTask = new SagaTimeoutTask(
                id,
                sagaId,
                name,
                new TimeoutExpired() {
                    @Override
                    public void expired(final Timeout timeout) {
                        timeoutExpired(timeout);
                    }
                },
                clock,
                data);

        ScheduledFuture future = scheduledService.schedule(timeoutTask, delay, timeUnit);

        synchronized (sync) {
            openTimeouts.put(id, sagaId, future);
        }

        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelTimeouts(final String sagaId) {
        checkNotNull(sagaId, "SagaId parameter must not be null.");

        synchronized (sync) {
            Collection<TimeoutId> timeoutsToRemove = new ArrayList<>();
            Map<TimeoutId, ScheduledFuture> sagaTimeouts = openTimeouts.column(sagaId);

            for (Map.Entry<TimeoutId, ScheduledFuture> timeout : sagaTimeouts.entrySet()) {
                timeout.getValue().cancel(false);
                timeoutsToRemove.add(timeout.getKey());
            }

            for (TimeoutId idToRemove : timeoutsToRemove) {
                openTimeouts.remove(idToRemove, sagaId);
            }
        }
    }

    @Override
    public void cancelTimeout(final TimeoutId id) {
        synchronized (sync) {
            Map<String, ScheduledFuture> timeouts = openTimeouts.row(id);
            Collection<String> sagaIdForRemoval = new ArrayList<>(1);

            for (Map.Entry<String, ScheduledFuture> timeout : timeouts.entrySet()) {
                timeout.getValue().cancel(false);
                sagaIdForRemoval.add(timeout.getKey());
            }

            // there is only one saga id associated with a single timeout id
            // however, in theory the table api allows a list, so we iterate over
            // the whole collection.
            for (String sagaId : sagaIdForRemoval) {
                openTimeouts.remove(sagaId, id);
            }
        }
    }

    /**
     * Called by timeout task once timeout has expired.
     */
    private void timeoutExpired(final Timeout timeout) {
        try {
            for (TimeoutExpired callback : callbacks) {
                callback.expired(timeout);
            }
        } catch (Exception ex) {
            // catch all exceptions. otherwise calling timeout thread of timers thread pool will be terminated.
            LOG.error("Error handling timeout.", ex);
        }
    }
}