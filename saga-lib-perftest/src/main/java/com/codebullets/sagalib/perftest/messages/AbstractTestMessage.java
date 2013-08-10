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
package com.codebullets.sagalib.perftest.messages;

/**
 * Base class for all test messages.
 */
public class AbstractTestMessage {
    private String correlationId;
    private boolean sagaFinished;

    /**
     * Generates a new instance of AbstractTestMessage.
     */
    public AbstractTestMessage() {
    }

    public AbstractTestMessage(final String correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isSagaFinished() {
        return sagaFinished;
    }

    public void setSagaFinished(final boolean sagaFinished) {
        this.sagaFinished = sagaFinished;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(final String correlationId) {
        this.correlationId = correlationId;
    }
}