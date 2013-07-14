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

/**
 * Calls the {@code expiredCallback} method when executed.
 */
public class SagaTimeoutTask implements Runnable {
    private final String sagaId;
    private final String name;
    private final TimeoutExpired expiredCallback;
    private final Clock clock;
    private final Object data;

    /**
     * Generates a new instance of SagaTimeoutTask.
     */
    public SagaTimeoutTask(final String sagaId, final String name, final TimeoutExpired expiredCallback, final Clock clock, final Object data) {
        this.sagaId = sagaId;
        this.name = name;
        this.expiredCallback = expiredCallback;
        this.clock = clock;
        this.data = data;
    }

    /**
     * Called by timer as timeout expires.
     */
    @Override
    public void run() {
        Timeout timeout = Timeout.create(sagaId, name, clock.now(), data);
        expiredCallback.expired(timeout);
    }
}