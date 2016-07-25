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
package com.codebullets.sagalib.perftest.saga;

import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.KeyReaders;
import com.codebullets.sagalib.StartsSaga;
import com.codebullets.sagalib.perftest.messages.AbstractTestMessage;
import com.codebullets.sagalib.perftest.messages.StopSaga;
import com.codebullets.sagalib.perftest.messages.TestMessage1;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test saga.
 */
public class TestSaga1 extends AbstractTestSaga {

    @StartsSaga
    public void handleTestMessage(final TestMessage1 message) {
        state().setCorrelationId(message.getCorrelationId());
        if (message.isSagaFinished()) {
            setFinished();
        }
    }

    @EventHandler
    public void handleStopMessage(final StopSaga stopMessage) {
        setFinished();
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        Collection<KeyReader> readers = new ArrayList<>();

        readers.add(KeyReaders.forMessage(
                StopSaga.class,
                AbstractTestMessage::getCorrelationId));

        return readers;
    }
}