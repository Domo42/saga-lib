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
package com.codebullets.sagalib.context;

/**
 * Holds and controls the the state when executing one ore more
 * sagas based for a single message.
 */
public interface ExecutionContext {
    /**
     * Stops the execution of further message handlers for the current message.
     */
    void stopDispatchingCurrentMessageToHandlers();

    /**
     * Gets a value indicating whether message dispatching is to be stopped.
     */
    boolean dispatchingStopped();
}