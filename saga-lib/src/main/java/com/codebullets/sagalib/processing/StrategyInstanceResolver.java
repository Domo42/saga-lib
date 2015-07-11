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

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Lookup a specific resolver strategy using the strategy finder.
 */
public class StrategyInstanceResolver implements InstanceResolver {
    private final StrategyFinder strategyFinder;

    /**
     * Generates a new instance of StrategyInstanceResolver.
     */
    @Inject
    public StrategyInstanceResolver(final StrategyFinder strategyFinder) {
        this.strategyFinder = strategyFinder;
    }

    @Override
    public Collection<SagaInstanceInfo> resolve(final LookupContext context) {
        Collection<SagaInstanceInfo> allInstances = new ArrayList<>();
        Collection<ResolveStrategy> resolveStrategies = strategyFinder.find(context);

        for (ResolveStrategy strategy : resolveStrategies) {
            Collection<SagaInstanceInfo> resolvedInstances = strategy.resolve(context);
            allInstances.addAll(resolvedInstances);
        }

        return allInstances;
    }
}