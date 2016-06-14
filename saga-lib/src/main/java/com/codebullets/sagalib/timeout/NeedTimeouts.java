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
 * <p>Implement this interface on your saga to request the timeout manager. The
 * timeout manager can be used to request future timeouts to be triggered.</p>
 *
 * <p>The {@link com.codebullets.sagalib.AbstractSaga} already implements the
 * interface and will provides some convenient methods to request timeout handling.</p>
 */
public interface NeedTimeouts {
    /**
     * When a saga implements the {@link NeedTimeouts} interface this method
     * is called during creation providing access to configured timeouts manager instance.
     */
    void setTimeoutManager(TimeoutManager timeoutManager);
}