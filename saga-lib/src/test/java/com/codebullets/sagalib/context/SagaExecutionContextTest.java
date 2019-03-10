/*
 * Copyright 2018 Stefan Domnanovits
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

package com.codebullets.sagalib.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SagaExecutionContextTest {
    private SagaExecutionContext sut;

    @BeforeEach
    void initContextTests() {
        sut = new SagaExecutionContext();
    }

    @Test
    void hasBeenStored_storeRecorded_returnsTrue() {
        // given
        String sagaId = "theSagaId";
        sut.recordSagaStateStored(sagaId);

        // when
        boolean hasBeenStored = sut.hasBeenStored(sagaId);

        // then
        assertThat("Expected stored flag to be set.", hasBeenStored, is(true));
    }

    @Test
    void hasBeenStored_storeRecordedOtherId_returnsFalse() {
        // given
        String sagaId = "theSagaId";
        sut.recordSagaStateStored("anotherId");

        // when
        boolean hasBeenStored = sut.hasBeenStored(sagaId);

        // then
        assertThat("Expected stored flag to be false.", hasBeenStored, is(false));
    }

    @Test
    void hasBeenStored_storedInChildContext_returnsTrue() {
        // given
        String sagaId = "theSagaId";
        SagaExecutionContext childContext = new SagaExecutionContext();
        childContext.setParentContext(sut);
        childContext.recordSagaStateStored(sagaId);

        // when
        boolean hasBeenStored = sut.hasBeenStored(sagaId);

        // then
        assertThat("Expected stored flag to be set.", hasBeenStored, is(true));
    }

    @Test
    void hasBeenStored_storedInChildContext_returnsTrueOnChildContext() {
        // given
        String sagaId = "theSagaId";
        SagaExecutionContext childContext = new SagaExecutionContext();
        childContext.setParentContext(sut);
        childContext.recordSagaStateStored(sagaId);

        // when
        boolean hasBeenStored = childContext.hasBeenStored(sagaId);

        // then
        assertThat("Expected stored flag to be set.", hasBeenStored, is(true));
    }
}