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
package com.codebullets.sagalib.storage;

import com.codebullets.sagalib.SagaState;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores saga states in memory.
 */
public class MemoryStorage implements StateStorage {
    private final Object sync = new Object();

    private final Map<String, StateStorageItem> storedStates = new HashMap<>();
    private final Multimap<SagaMultiKey, SagaState> instanceKeyMap = HashMultimap.create();

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(final SagaState state) {
        checkNotNull(state, "State not allowed to be null.");
        checkNotNull(state.getSagaId(), "State saga id not allowed to be null.");
        checkNotNull(state.getType(), "Saga type must not be null.");

        synchronized (sync) {
            String sagaId = state.getSagaId();

            StateStorageItem stateStorageItem = storedStates.get(sagaId);
            if (stateStorageItem == null) {
                stateStorageItem = StateStorageItem.withCurrentInstanceKeys(state);
                storedStates.put(state.getSagaId(), stateStorageItem);
            } else {
                // remove previous stored keys from map
                // some entries may have been removed from the state during
                // saga execution
                removeInstancesForItem(stateStorageItem);

                // once old values have been removed update item with current
                // values
                stateStorageItem.updateInstanceKeys();
            }

            for (SagaMultiKey key : stateStorageItem.instanceKeys()) {
                instanceKeyMap.put(key, state);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SagaState load(final String sagaId) {
        checkNotNull(sagaId, "Saga id key must be set.");

        SagaState state = null;
        synchronized (sync) {
            StateStorageItem storage = storedStates.get(sagaId);
            if (storage != null) {
                state = storage.sagaState();
            }
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String sagaId) {
        checkNotNull(sagaId, "Saga id key must be set.");

        synchronized (sync) {
            StateStorageItem removedItem = storedStates.remove(sagaId);
            if (removedItem != null) {
                removeInstancesForItem(removedItem);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<? extends SagaState> load(final String type, final Object instanceKey) {
        Collection<? extends SagaState> items;

        synchronized (sync) {
            items = new ArrayList<>(instanceKeyMap.get(SagaMultiKey.create(type, instanceKey)));
        }

        return items;
    }

    private void removeInstancesForItem(final StateStorageItem stateStorageItem) {
        for (SagaMultiKey key : stateStorageItem.instanceKeys()) {
            instanceKeyMap.removeAll(key);
        }
    }

    /**
     * Combined key of saga type and saga key instance value.
     */
    private static final class SagaMultiKey {
        private final String type;
        private final Object instanceKey;

        SagaMultiKey(final String type, final Object instanceKey) {
            checkNotNull(type, "type must not be null");

            this.type = type;
            this.instanceKey = instanceKey;
        }

        @Override
        public boolean equals(final Object o) {
            boolean isEqual = false;

            if (o instanceof SagaMultiKey) {
                SagaMultiKey other = (SagaMultiKey) o;
                isEqual = Objects.equals(type, other.type)
                       && Objects.equals(instanceKey, other.instanceKey);
            }

            return isEqual;
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, instanceKey);
        }

        public static SagaMultiKey create(final String type, final Object instanceKey) {
            return new SagaMultiKey(type, instanceKey);
        }
    }

    /**
     * Encapsulates the saga state as well as a separate copy of instance
     * keys, originally provided by the saga state.
     */
    private static final class StateStorageItem {
        private final SagaState sagaState;
        private Collection<SagaMultiKey> instanceKeys;

        /**
         * Create a new storage item create a separate copy of the list
         * of associated instance keys.
         */
        public static StateStorageItem withCurrentInstanceKeys(final SagaState sagaState) {
            return new StateStorageItem(sagaState);
        }

        private StateStorageItem(final SagaState sagaState) {
            this.sagaState = sagaState;
            instanceKeys = createSagaKeys(sagaState);
        }

        public SagaState sagaState() {
            return sagaState;
        }

        /**
         * Instruct storage object to update its own private copy of instance keys.
         */
        public void updateInstanceKeys() {
            instanceKeys = createSagaKeys(sagaState);
        }

        public Iterable<SagaMultiKey> instanceKeys() {
            return instanceKeys;
        }

        private Collection<SagaMultiKey> createSagaKeys(final SagaState state) {
            Collection<SagaMultiKey> keys = new ArrayList<>(state.instanceKeys().size());
            for (Object key : state.instanceKeys()) {
                keys.add(SagaMultiKey.create(state.getType(), key));
            }

            return keys;
        }
    }
}