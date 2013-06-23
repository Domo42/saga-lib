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

import java.util.concurrent.TimeUnit;

/**
 * Triggers timeout events after the time they have been requested.
 */
public interface TimeoutManager {
    /**
     * Registers a callback handler for expired timeouts.
     * @param callback The method to call as the timeout has expired.
     */
    void addExpiredCallback(TimeoutExpired callback);

    /**
     * Request a timeout event to be triggered in the future.
     * @param sagaId The id of the saga requesting the timeout.
     * @param name A custom name for the timeout. Is returned once timeout expired.
     * @param delay Time to wait until timeout expires.
     * @param timeUnit Specifies the unit of the {@code delay} argument.
     */
    void requestTimeout(String sagaId, String name, long delay, TimeUnit timeUnit);

    /**
     * Cancel all timeouts of a saga. If no timeout exist does nothing.
     */
    void cancelTimeouts(String sagaId);
}
