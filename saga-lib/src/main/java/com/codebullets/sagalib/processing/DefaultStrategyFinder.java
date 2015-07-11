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

import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.Timeout;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Default implementation to map a specific message to saga instances.
 * <p>There are specific strategies whether the source message is a timeout, is starting
 * a new saga or continues an existing one.</p>
 */
public class DefaultStrategyFinder implements StrategyFinder {
    private final TypesForMessageMapper typesForMessageMapper;
    private final SagaInstanceFactory instanceFactory;
    private final KeyExtractor keyExtractor;
    private final StateStorage stateStorage;

    /**
     * Generates a new instance of DefaultStrategyFinder.
     */
    @Inject
    public DefaultStrategyFinder(
            final TypesForMessageMapper typesForMessageMapper,
            final SagaInstanceFactory instanceFactory,
            final KeyExtractor keyExtractor,
            final StateStorage stateStorage) {
        this.typesForMessageMapper = typesForMessageMapper;
        this.instanceFactory = instanceFactory;
        this.keyExtractor = keyExtractor;
        this.stateStorage = stateStorage;
    }

    @Override
    public Collection<ResolveStrategy> find(final LookupContext context) {
        Collection<ResolveStrategy> strategies = new ArrayList<>();

        // timeout is special, saga id is known resulting in a simpler strategy without
        // having to read instance keys or searching by instance key in the state storage.
        if (context.message() instanceof Timeout) {
            strategies.add(new TimeoutResolveStrategy(typesForMessageMapper, instanceFactory, stateStorage));
        } else {
            strategies.addAll(checkAnnotatedMethodTypes(context));
        }

        return strategies;
    }

    private Collection<ResolveStrategy> checkAnnotatedMethodTypes(final LookupContext context) {
        Collection<ResolveStrategy> strategies = new ArrayList<>();
        Collection<SagaType> sagasToExecute = typesForMessageMapper.getSagasForMessageType(context.message().getClass());

        for (SagaType type : sagasToExecute) {
            if (type.isStartingNewSaga()) {
                strategies.add(new StartNewSagaStrategy(type, instanceFactory));
            } else {
                strategies.add(new ContinueSagaStrategy(type, instanceFactory, keyExtractor, stateStorage));
            }
        }

        return strategies;
    }
}