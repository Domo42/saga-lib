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
package com.codebullets.sagalib;

/**
 * Abstract state implementation already containing the basic
 * properties expected from {@link SagaState}.<p/>
 * The instance key method is intentionally missing. This one should
 * be modelled and implemented individually for each saga.
 */
public abstract class AbstractSagaState implements SagaState {
    private String sagaId;
    private String sagaType;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSagaId() {
        return sagaId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSagaId(final String sagaId) {
        this.sagaId = sagaId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return sagaType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setType(final String type) {
        sagaType = type;
    }
}