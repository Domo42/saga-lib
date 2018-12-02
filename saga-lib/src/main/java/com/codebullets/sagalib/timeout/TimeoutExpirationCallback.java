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

package com.codebullets.sagalib.timeout;

/**
 * Extends the original expired interface with a callback containing
 * additional context information.
 *
 * <p>When provided as callback to the timeout manager this callback
 * method will be executed instead of the one from the base interface.</p>
 *
 * <p>This weird inheritance and behavior is done to keep compatibility
 * with previous saga-lib versions.</p>
 */
public interface TimeoutExpirationCallback extends TimeoutExpired {
    /**
     * Called as a timeout has expired.
     */
    void expired(Timeout timeout, TimeoutExpirationContext context);
}
