/*
 * Copyright 2016 Stefan Domnanovits
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.codebullets.sagalib;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * Always returns the same state values, representing an
 * invalid saga state.
 */
final class InvalidState extends AbstractSagaState {
    static final InvalidState INSTANCE = new InvalidState();
    private static final Set<String> INSTANCE_KEYS = Collections.unmodifiableSet(Sets.newHashSet("InvalidState"));

    @Override
    public String getSagaId() {
        return "AbstractSingleEventSaga";
    }

    @Override
    public String getType() {
        return "AbstractSingleEventSaga";
    }

    @Override
    public Set<String> instanceKeys() {
        return INSTANCE_KEYS;
    }
}
