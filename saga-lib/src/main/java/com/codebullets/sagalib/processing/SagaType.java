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
 * Describes the saga and expected handling.
 */
public final class SagaType {
    private boolean startsNew;
    private Class<? extends Saga> sagaClass;
    private Object instanceKey;
    private String sagaId;

    /**
     * Use static builder methods to create instance.
     */
    private SagaType() {
    }

    /**
     * Indicates a new saga needs to be started.
     */
    public boolean isStartingNewSaga() {
        return startsNew;
    }

    /**
     * Gets the type of the saga to create.
     */
    public Class<? extends Saga> getSagaClass() {
        return sagaClass;
    }

    /**
     * Gets the instance key of a running saga.
     */
    public Object getInstanceKey() {
        return instanceKey;
    }

    /**
     * Gets the id of the saga if known from source message.
     */
    public String getSagaId() {
        return sagaId;
    }

    /**
     * Creates a type indicating to start a complete new saga.
     */
    public static SagaType startsNewSaga(final Class<? extends Saga> sagaClass) {
        SagaType sagaType = new SagaType();
        sagaType.startsNew = true;
        sagaType.sagaClass = sagaClass;
        sagaType.sagaId = null;
        sagaType.instanceKey = null;

        return sagaType;
    }

    /**
     * Creates a type continuing a saga as result of a timeout message.
     */
    public static SagaType sagaFromTimeout(final String sagaId) {
        SagaType sagaType = new SagaType();
        sagaType.startsNew = false;
        sagaType.sagaClass = null;
        sagaType.sagaId = sagaId;
        sagaType.instanceKey = null;

        return sagaType;
    }

    /**
     * Creates a type continuing a saga from a message based on a specific instance key.
     */
    public static SagaType continueSaga(final SagaType originalType, final Object instanceKey) {
        SagaType sagaType = new SagaType();
        sagaType.startsNew = false;
        sagaType.sagaClass = originalType.getSagaClass();
        sagaType.sagaId = null;
        sagaType.instanceKey = instanceKey;

        return sagaType;
    }

    /**
     * Creates a type continuing a saga with an instance key that has yet to be defined.
     */
    public static SagaType continueSaga(final Class<? extends Saga> sagaClass) {
        SagaType sagaType = new SagaType();
        sagaType.startsNew = false;
        sagaType.sagaClass = sagaClass;
        sagaType.sagaId = null;

        return sagaType;
    }

    @Override
    public String toString() {
        return sagaClass != null ? sagaClass.getSimpleName() : "SagaId=" + sagaId;
    }
}