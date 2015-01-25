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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract state implementation already containing the basic
 * properties expected from {@link SagaState}.<p/>
 * The instance key method is intentionally missing. This one should
 * be modelled and implemented individually for each saga.
 * @param <KEY> The type of the instance to match state and messages.
 */
public abstract class AbstractSagaState<KEY> implements SagaState<KEY>, Serializable {
    private static final long serialVersionUID = 1L;

    private String sagaId;
    private String sagaType;
    private final Set<KEY> instanceKeys = new HashSet<>(8);

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

    /**
     * Adds a single instance key to the list of matched keys.
     */
    public void addInstanceKey(final KEY key) {
        instanceKeys.add(key);
    }

    /**
     * Removes a single instance from the list of matching keys.
     */
    public void removeInstanceKey(final KEY key) {
        instanceKeys.remove(key);
    }

    /**
     * {@inheritDoc}
     * <p>This set returns the keys used with {@link #addInstanceKey(Object)}.
     */
    @Override
    public Set<KEY> instanceKeys() {
        return Collections.unmodifiableSet(instanceKeys);
    }
}