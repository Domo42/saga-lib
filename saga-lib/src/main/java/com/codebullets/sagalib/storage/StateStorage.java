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

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Saves and loads the state of a saga.
 */
public interface StateStorage {
    /**
     * Save the state so that in can be retrieved later on by the {@link #load(String)} method.
     * @param state The saga state to save.
     */
    void save(SagaState state);

    /**
     * Load a single saga state based on unique saga id.
     * @return Returns the available saga instance. Returns null if nothing has been found.
     */
    SagaState load(String sagaId);

    /**
     * Delete the state of a saga. If no entry exists nothing happens.
     */
    void delete(String sagaId);

    /**
     * Load a list of saga states based on saga type and instance id. The instance id is chosen
     * by the saga implementer and should in ideal cases only return on instance inside the collection.
     *
     * @return List of found saga state instances.
     */
    Collection<? extends SagaState> load(String type, Object instanceKey);

    /**
     * Load a list of saga states based on a list of parameters consisting of the
     * saga type and instance key. This method has been introduced to enable the
     * use of only a single operation against an external data store for all states
     * of a message.
     *
     * <p>The default implementation is calling {@link #load(String, Object)} for each parameter
     * individually and collects the results into a single stream.</p>
     */
    default Stream<? extends SagaState> loadAll(final Iterable<InstanceKeySearchParam> searchParams) {
        return StreamSupport.stream(searchParams.spliterator(), false)
                .flatMap(param -> load(param.getSagaTypeName(), param.getInstanceKey()).stream());
    }
}
