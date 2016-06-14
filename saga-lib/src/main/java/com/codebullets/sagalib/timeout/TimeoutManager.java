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

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * <p>Triggers timeout events after the time they have been requested.</p>
 *
 * <p>A saga can handle those timeouts by declaring a message handler using
 * the {@link com.codebullets.sagalib.EventHandler} annotation on a
 * method with a single parameter of type {@link Timeout}.</p>
 */
public interface TimeoutManager {
    /**
     * Registers a callback handler for expired timeouts.
     * @param callback The method to call as the timeout has expired.
     */
    void addExpiredCallback(TimeoutExpired callback);

    /**
     * Request a timeout event to be triggered in the future.
     * @param context The context the timeout was requested from.
     * @param sagaId The id of the saga requesting the timeout.
     * @param delay Time to wait until timeout expires.
     * @param timeUnit Specifies the unit of the {@code delay} argument.
     * @param name A custom name for the timeout. Is returned as timeout has expired.
     * @param data Optional data object associated with the timeout. Can be null.
     */
    TimeoutId requestTimeout(ExecutionContext context, String sagaId, long delay, TimeUnit timeUnit, @Nullable String name, @Nullable Object data);

    /**
     * Cancel all timeouts of a saga. If no timeout exists does nothing.
     */
    void cancelTimeouts(String sagaId);

    /**
     * Cancel a specific timeout. If no timeout is found does nothing.
     */
    void cancelTimeout(final TimeoutId id);
}
