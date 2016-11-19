/*
 * Copyright 2016 Stefan Domnanovits
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

package com.codebullets.sagalib.processing;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This exception is thrown in case multiple errors have occurred when handling
 * a message. In case of a single error, the original exception is re-thrown as is.
 *
 * <p>This encapsulated list might includes errors from executing saga as well
 * has module handlers.</p>
 */
public class SagaExecutionErrorsException extends RuntimeException {
    private final Collection<Exception> executionErrors;

    /**
     * Creates a new exceptions indicating multiple errors during sage handling.
     */
    public SagaExecutionErrorsException(final String message, final Collection<Exception> errors) {
        super(message);
        this.executionErrors = new ArrayList<>(errors);
    }

    /**
     * Gets the list of encountered exceptions handling a message.
     */
    public Collection<Exception> getExecutionErrors() {
        return executionErrors;
    }

    private static String encodeExceptionMessage(final String orgMessage, final Collection<Exception> errors) {
        StringBuilder sb = new StringBuilder(orgMessage);

        return sb.toString();
    }
}
