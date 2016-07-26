/*
 * Copyright 2016 Stefan Domnanovits
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
import com.codebullets.sagalib.storage.InstanceKeySearchParam;
import com.codebullets.sagalib.storage.StateStorage;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves all continue saga instances with a single call to the state storage.
 */
public class ContinueAllStrategy implements ResolveStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(ContinueAllStrategy.class);

    private final Collection<SagaType> continueSagaTypes;
    private final SagaInstanceFactory instanceFactory;
    private final KeyExtractor keyExtractor;
    private final StateStorage stateStorage;

    /**
     * Generates a new instance of ContinueAllStrategy.
     */
    public ContinueAllStrategy(
            final Collection<SagaType> continueSagaTypes,
            final SagaInstanceFactory instanceFactory,
            final KeyExtractor keyExtractor,
            final StateStorage stateStorage) {
        this.continueSagaTypes = continueSagaTypes;
        this.instanceFactory = instanceFactory;
        this.keyExtractor = keyExtractor;
        this.stateStorage = stateStorage;
    }

    @Override
    public Collection<SagaInstanceInfo> resolve(final LookupContext context) {
        // for continuation, the instance keys are important.
        Collection<InstanceKeySearchParam> instanceKeys = extractInstanceKeys(context);

        // load all saga states with a single state storage operation
        Stream<? extends SagaState> sagaStates = stateStorage.loadAll(instanceKeys);

        return sagaStates.map(this::continueExistingSaga).collect(Collectors.toList());
    }

    private Collection<InstanceKeySearchParam> extractInstanceKeys(final LookupContext context) {
        Collection<InstanceKeySearchParam> instanceKeys = new ArrayList<>(continueSagaTypes.size());

        continueSagaTypes.forEach(sagaType -> {
            Object key = readInstanceKey(sagaType, context);
            if (key != null) {
                instanceKeys.add(new InstanceKeySearchParam(sagaType.getSagaClass().getName(), key));
            } else {
                LOG.debug("Can not determine saga instance key from message {}", context.message().getClass());
            }
        });

        return instanceKeys;
    }

    /**
     * Create a new saga instance with already existing saga state.
     */
    private SagaInstanceInfo continueExistingSaga(final SagaState sagaState) {
        SagaInstanceInfo instanceInfo;

        try {
            Saga saga = instanceFactory.continueExisting(sagaState.getType(), sagaState);
            instanceInfo = SagaInstanceInfo.define(saga, false);
        } catch (ExecutionException e) {
            Throwables.propagate(e);
            instanceInfo = null;
        }

        return instanceInfo;
    }

    private Object readInstanceKey(final SagaType sagaType, final LookupContext context) {
        return keyExtractor.findSagaInstanceKey(sagaType.getSagaClass(), context);
    }
}