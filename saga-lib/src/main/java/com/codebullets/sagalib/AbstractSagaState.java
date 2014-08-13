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

import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract state implementation already containing the basic
 * properties expected from {@link SagaState}.<p/>
 * The instance key method is intentionally missing. This one should
 * be modelled and implemented individually for each saga.
 */
public abstract class AbstractSagaState implements SagaState, Serializable {
    private static final long serialVersionUID = 1L;

    private String sagaId;
    private String sagaType;
    private final Set<String> instanceKeys = new HashSet<>(8);

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
    public void addInstanceKey(final String key) {
        instanceKeys.add(key);
    }

    /**
     * Removes a single instance from the list of matching keys.
     */
    public void removeInstanceKey(final String key) {
        instanceKeys.remove(key);
    }

    @Override
    @Deprecated
    public String instanceKey() {
        String key = null;

        if (!instanceKeys.isEmpty()) {
            key = instanceKeys.iterator().next();
        }

        return key;
    }

    /**
     * {@inheritDoc}
     * <p>This set returns the keys used with {@link #addInstanceKey(String)} or the
     * deprecated {@link SagaState#instanceKey()} if the set is empty.</p>
     */
    @Override
    public Set<String> instanceKeys() {
        Set<String> keys;

        // this is for backwards compatibility. If local list is empty
        // assume implementation returns single key. Use this single key
        // instead of empty list.
        if (instanceKeys.isEmpty()) {
            keys = Sets.newHashSet(instanceKey());
        } else {
            keys = instanceKeys;
        }

        return keys;
    }
}