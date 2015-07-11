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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Starts a new saga for handling the specific message.
 */
public class StartNewSagaStrategy implements ResolveStrategy {
    private final SagaInstanceFactory instanceFactory;
    private final SagaType typeToCreate;

    /**
     * Generates a new instance of StartNewSagaStrategy.
     */
    public StartNewSagaStrategy(final SagaType typeToCreate, final SagaInstanceFactory instanceFactory) {
        this.typeToCreate = typeToCreate;
        this.instanceFactory = instanceFactory;
    }

    @Override
    public Collection<SagaInstanceInfo> resolve(final LookupContext context) {
        Collection<SagaInstanceInfo> instances = new ArrayList<>(1);
        SagaInstanceInfo instance = instanceFactory.createNew(typeToCreate);
        if (instance != null) {
            instances.add(instance);
        }

        return instances;
    }
}