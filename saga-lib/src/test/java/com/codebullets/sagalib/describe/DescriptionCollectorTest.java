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

package com.codebullets.sagalib.describe;

import com.codebullets.sagalib.timeout.Timeout;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

public class DescriptionCollectorTest {
    @Test
    public void handle_singleStartHandler_executesStartHandler() {
        // given
        boolean[] handlerExecuted = new boolean[1];
        HandlerDescription description = HandlerDescriptions.startedBy(String.class).usingMethod(s -> handlerExecuted[0] = true)
                .finishDescription();

        // when
        description.handler().accept("any");

        // then
        assertThat("Expected the start handler to be executed.", handlerExecuted[0], is(true));
    }

    @Test
    public void handle_startAndContinueHandlerCallContinue_executesOnlyContinueHandler() {
        // given
        boolean[] startHandlerExecuted = new boolean[1];
        boolean[] continueHandlerExecuted = new boolean[1];
        HandlerDescription description = HandlerDescriptions
                .startedBy(String.class).usingMethod(s -> startHandlerExecuted[0] = true)
                .handleMessage(Integer.class).usingMethod(i -> continueHandlerExecuted[0] = true)
                .finishDescription();

        // when
        description.handler().accept(42);

        // then
        assertThat("Expected the start handler not to be executed.", startHandlerExecuted[0], is(false));
        assertThat("Expected the continue handler to be executed.", continueHandlerExecuted[0], is(true));
    }

    @Test
    public void handle_startAndContinueHandlerCallStart_executesOnlyStartHandler() {
        // given
        boolean[] startHandlerExecuted = new boolean[1];
        boolean[] continueHandlerExecuted = new boolean[1];
        HandlerDescription description = HandlerDescriptions
                .startedBy(String.class).usingMethod(s -> startHandlerExecuted[0] = true)
                .handleMessage(Integer.class).usingMethod(i -> continueHandlerExecuted[0] = true)
                .finishDescription();

        // when
        description.handler().accept("start");

        // then
        assertThat("Expected the start handler to be executed.", startHandlerExecuted[0], is(true));
        assertThat("Expected the continue handler not to be executed.", continueHandlerExecuted[0], is(false));
    }

    @Test
    public void handlerTypes_startAndContinueHandler_handlerListHasStartAndContinue() {
        // given
        HandlerDescription description = HandlerDescriptions
                .startedBy(String.class).usingMethod(s -> {})
                .handleMessage(Integer.class).usingMethod(i -> {})
                .finishDescription();

        // when
        Iterable<Class<?>> handlerTypes = description.handlerTypes();

        // then
        assertThat("Expected the start type in handlers.", handlerTypes, hasItem(String.class));
        assertThat("Expected the continue type in handlers.", handlerTypes, hasItem(Integer.class));
    }

    @Test
    public void startedBy_startAndContinueHandler_startedByMatchesConfig() {
        // given
        HandlerDescription description = HandlerDescriptions
                .startedBy(String.class).usingMethod(s -> {})
                .handleMessage(Integer.class).usingMethod(i -> {})
                .finishDescription();

        // when
        Class<?> startedBy = description.startedBy();

        // then
        assertThat("Expected the start type in startedBy description.", startedBy, sameInstance(String.class));
    }

    @Test
    public void handle_startContinueTimeout_executesOnlyTimeout() {
        // given
        boolean[] startHandlerExecuted = new boolean[1];
        boolean[] continueHandlerExecuted = new boolean[1];
        boolean[] timeoutExecuted = new boolean[1];
        HandlerDescription description = HandlerDescriptions
                .startedBy(String.class).usingMethod(s -> startHandlerExecuted[0] = true)
                .handleMessage(Integer.class).usingMethod(i -> continueHandlerExecuted[0] = true)
                .handleMessage(Timeout.class).usingMethod(t -> timeoutExecuted[0] = true)
                .finishDescription();

        // when
        description.handler().accept(Timeout.create(null, null, null, null));

        // then
        assertThat("Expected the start handler not to be executed.", startHandlerExecuted[0], is(false));
        assertThat("Expected the continue handler not to be executed.", continueHandlerExecuted[0], is(false));
        assertThat("Expected the timeout handler to be executed.", timeoutExecuted[0], is(true));
    }
}