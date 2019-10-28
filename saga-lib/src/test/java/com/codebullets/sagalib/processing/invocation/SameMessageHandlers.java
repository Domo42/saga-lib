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

package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.StartsSaga;
import com.codebullets.sagalib.TestSagaState;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

/**
 * Test saga using both a start and continue handler with the same message argument.
 */
public class SameMessageHandlers extends AbstractSaga<TestSagaState> {
    private boolean startHandlerCalled;
    private boolean continueHandlerCalled;

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    public boolean isStartHandlerCalled() {
        return startHandlerCalled;
    }

    public boolean isContinueHandlerCalled() {
        return continueHandlerCalled;
    }

    @StartsSaga
    public void startSaga(final String message) {
        startHandlerCalled = true;
    }

    @EventHandler
    public void continueSaga(final String message) {
        continueHandlerCalled = true;
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return ImmutableSet.of();
    }
}
