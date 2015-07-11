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
import com.codebullets.sagalib.timeout.Timeout;
import com.google.common.base.Throwables;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * Resolves the saga instances associated with a timeout. Timeouts
 * will most likely continue existing saga (that have requested a timeout),
 * but may also be handled separate sagas started by timeouts.
 */
public class TimeoutResolveStrategy implements ResolveStrategy {
    private final TypesForMessageMapper typesForMessageMapper;
    private final SagaInstanceFactory sagaFactory;
    private final StateStorage storage;

    /**
     * Generates a new instance of TimeoutResolveStrategy.
     */
    @Inject
    public TimeoutResolveStrategy(final TypesForMessageMapper typesForMessageMapper, final SagaInstanceFactory creator, final StateStorage storage) {
        this.typesForMessageMapper = typesForMessageMapper;
        this.sagaFactory = creator;
        this.storage = storage;
    }

    @Override
    public Collection<SagaInstanceInfo> resolve(final LookupContext context) {
        Timeout timeout = (Timeout) context.message();

        Collection<SagaType> sagaTypes = prepareTypesList();
        Collection<SagaInstanceInfo> instances = new ArrayList<>(sagaTypes.size() + 1);

        for (SagaType sagaType : sagaTypes) {
            SagaInstanceInfo instance = sagaFactory.createNew(sagaType);
            if (instance != null) {
                instances.add(instance);
            }
        }

        SagaInstanceInfo continueInstance = continueForExistingSaga(timeout);
        if (continueInstance != null) {
            instances.add(continueInstance);
        }

        return instances;
    }

    private SagaInstanceInfo continueForExistingSaga(final Timeout timeout) {
        SagaInstanceInfo instance = null;

        try {
            SagaState sagaState = storage.load(timeout.getSagaId());
            if (sagaState != null) {
                Saga saga = sagaFactory.continueExisting(sagaState.getType(), sagaState);
                instance = SagaInstanceInfo.define(saga, false);
            }
        } catch (ExecutionException e) {
            Throwables.propagate(e);
        }

        return instance;
    }

    /**
     * Timeouts are special. They do not need an instance key to be found. However
     * there may be other starting sagas that want to handle timeouts of other saga instances.
     */
    private Collection<SagaType> prepareTypesList() {
        Collection<SagaType> sagaTypes = new ArrayList<>();

        // search for other sagas started by timeouts
        Collection<SagaType> sagasToExecute = typesForMessageMapper.getSagasForMessageType(Timeout.class);
        for (SagaType type : sagasToExecute) {
            if (type.isStartingNewSaga()) {
                sagaTypes.add(type);
            }
        }

        return sagaTypes;
    }
}