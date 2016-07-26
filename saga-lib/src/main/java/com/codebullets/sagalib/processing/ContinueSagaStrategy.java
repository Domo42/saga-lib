/*
 * Copyright 2015 Stefan Domnanovits
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
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.storage.StateStorage;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Continues a specific saga, by resolving the instance key and searching for
 * existing saga states.
 *
 * @deprecated The {@link ContinueAllStrategy} allows for increased performance by
 *             reducing the number of needed storage operations.
 */
@Deprecated
public class ContinueSagaStrategy implements ResolveStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(ContinueSagaStrategy.class);

    private final SagaType typeToCreate;
    private final SagaInstanceFactory instanceFactory;
    private final KeyExtractor keyExtractor;
    private final StateStorage stateStorage;

    /**
     * Generates a new instance of ContinueSagaStrategy.
     */
    public ContinueSagaStrategy(
            final SagaType typeToCreate,
            final SagaInstanceFactory instanceFactory,
            final KeyExtractor keyExtractor,
            final StateStorage stateStorage) {
        this.typeToCreate = typeToCreate;
        this.instanceFactory = instanceFactory;
        this.keyExtractor = keyExtractor;
        this.stateStorage = stateStorage;
    }

    @Override
    public Collection<SagaInstanceInfo> resolve(final LookupContext context) {
        Collection<SagaInstanceInfo> instances = new ArrayList<>();

        // for continuation, the instance key is important.
        Object key = readInstanceKey(typeToCreate, context);
        if (key != null) {
            instances.addAll(continueExistingSaga(typeToCreate, key));
        } else {
            LOG.debug("Can not determine saga instance key from message {}", context.message().getClass());
        }

        return instances;
    }

    /**
     * Create a new saga instance with already existing saga state.
     */
    private Collection<SagaInstanceInfo> continueExistingSaga(final SagaType sagaType, final Object instanceKey) {
        Collection<SagaInstanceInfo> sagas = new ArrayList<>();

        Collection<? extends SagaState> sagaStates = stateStorage.load(sagaType.getSagaClass().getName(), instanceKey);
        for (SagaState sagaState : sagaStates) {
            try {
                Saga saga = instanceFactory.continueExisting(sagaType.getSagaClass(), sagaState);
                sagas.add(SagaInstanceInfo.define(saga, false));
            } catch (ExecutionException e) {
                Throwables.propagate(e);
            }
        }

        return sagas;
    }

    private Object readInstanceKey(final SagaType sagaType, final LookupContext context) {
        return keyExtractor.findSagaInstanceKey(sagaType.getSagaClass(), context);
    }
}