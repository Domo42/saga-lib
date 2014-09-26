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
 * This message is used by the saga-lib in case no saga has been found to
 * handle a particular message.
 * <p>Users of the saga-lib can write a saga that handles this specific
 * message and perform custom logic like writing a warning.</p>
 * <p>In case there is no DeadMessage handler the saga-lib will log a warning
 * on its own. With a handler the saga-lib will stay silent.</p>
 */
public class DeadMessage {
    private final Object originalMessage;

    /**
     * Generates a new instance of DeadMessage.
     */
    public DeadMessage(final Object originalMessage) {
        this.originalMessage = originalMessage;
    }

    /**
     * Returns the original message, to which no matching saga handler
     * has been found.
     */
    public Object originalMessage() {
        return originalMessage;
    }
}