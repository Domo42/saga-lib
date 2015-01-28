/*
 * Copyright 2014 Stefan Domnanovits
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
package com.codebullets.sagalib.keyintegration;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.KeyExtractFunction;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.KeyReaders;
import com.codebullets.sagalib.StartsSaga;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class StatefulSaga extends AbstractSaga<UuidSagaState> {
    private final TestState testState;

    /**
     * Generates a new instance of StatefulSaga.
     */
    public StatefulSaga(final TestState testState) {
        this.testState = testState;
    }

    @Override
    public void createNewState() {
        setState(new UuidSagaState());
    }

    @StartsSaga
    public void startingSaga(final String startingMessage) {
        state().addInstanceKey(UUID.fromString(startingMessage));
    }

    @EventHandler
    public void handleResponse(final ResponseMessage response) {
        testState.setResponseHandled(true);
        setFinished();
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        ArrayList<KeyReader> readers = new ArrayList<>();
        readers.add(KeyReaders.forMessage(
                        ResponseMessage.class,
                        new KeyExtractFunction<ResponseMessage, UUID>() {
                            @Override
                            public UUID key(final ResponseMessage response) {
                                return response.getResponseId();
                            }
                        }));

        return readers;
    }
}