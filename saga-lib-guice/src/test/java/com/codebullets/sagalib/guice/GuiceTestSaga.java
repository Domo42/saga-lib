/*
 * Copyright 2013 Stefan Domnanovits
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
package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.StartsSaga;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Test saga using guice creation and dependency injection.
 */
public class GuiceTestSaga extends AbstractSaga<TestSagaState> {

    private final SagaMonitor monitor;

    /**
     * Generates a new instance of GuiceTestSaga.
     */
    @Inject
    public GuiceTestSaga(final SagaMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * This will start the saga if a message of type String is added to
     * the message stream.
     */
    @StartsSaga
    public void startTheSaga(final String sagaParam) {
        monitor.setSagaHasStarted();
        state().setSagaParam(sagaParam);
    }

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return new ArrayList<>();
    }
}