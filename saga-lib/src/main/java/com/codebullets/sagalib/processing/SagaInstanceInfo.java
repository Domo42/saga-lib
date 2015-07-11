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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;

/**
 * Holds the created saga as well as additional creation information.
 */
public class SagaInstanceInfo {
    private final Saga saga;
    private final boolean starting;

    /**
     * Generates a new instance of SagaInstanceDescription.
     */
    public SagaInstanceInfo(final Saga saga, final boolean starting) {
        this.saga = saga;
        this.starting = starting;
    }

    /**
     * Gets the created saga instance.
     */
    public Saga getSaga() {
        return saga;
    }

    /**
     * Gets a value indicating whether the saga is currently starting.
     * @return True if saga is starting; false if existed before and is continuing an existing workflow.
     */
    public boolean isStarting() {
        return starting;
    }

    /**
     * Create a new instance description containing the actual instance
     * and the information whether the saga is currently starting or continuing
     * and existing workflow.
     */
    public static SagaInstanceInfo define(final Saga saga, final boolean isStarting) {
        return new SagaInstanceInfo(saga, isStarting);
    }
}