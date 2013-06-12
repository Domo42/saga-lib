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

import com.codebullets.sagalib.messages.Timeout;

/**
 * Triggers timeout events after the time they have been requested.
 */
public interface TimeoutManager {
    /**
     * Request a timeout event to be triggered in the future.
     * @param timeout Contains the timeout data.
     * @param callback The method to call as the timeout has expired.
     */
    void requestTimeout(Timeout timeout, TimeoutExpired callback);
}
