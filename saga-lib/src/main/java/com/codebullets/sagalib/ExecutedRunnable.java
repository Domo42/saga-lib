/*
 * Copyright 2014 Stefan Domnanovits
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
package com.codebullets.sagalib;

/**
 * This is the runnable executed by the saga lib. The provided executor
 * is responsible to call the {@code run()} method to trigger the individual
 * saga handlers.
 * <p>By default all sagas are executed by a the Executor returned from a
 * {@code Executors.newSingleThreadExecutor()} call.</p>
 */
public interface ExecutedRunnable extends Runnable {
    /**
     * Returns the message that triggered the saga execution handling.
     */
    Object message();
}