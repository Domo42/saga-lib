/*
 * Copyright 2016 Stefan Domnanovits
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
package com.codebullets.sagalib.handling;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.KeyReaders;
import com.codebullets.sagalib.TestSagaState;
import com.codebullets.sagalib.describe.DescribesHandlers;
import com.codebullets.sagalib.describe.HandlerDescription;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Map;

import static com.codebullets.sagalib.describe.HandlerDescriptions.startedBy;

public class DirectDescriptionSaga extends AbstractSaga<TestSagaState> implements DescribesHandlers {
    static final String START_CALLED_KEY = "DirectDescriptionSaga.start.called.key";
    static final String CONTINUE_CALLED_KEY = "DirectDescriptionSaga.continue.called.key";

    private final Map<String, String> context;

    public DirectDescriptionSaga(Map<String, String> context) {
        this.context = context;
    }

    private void startingSaga(final String startingMessage) {
        context.put(START_CALLED_KEY, "true");
        state().addInstanceKey(startingMessage);
    }

    private void continueSaga(final Integer intMessage) {
        context.put(CONTINUE_CALLED_KEY, "true");
        setFinished();
    }

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return ImmutableSet.of(
                KeyReaders.forMessage(Integer.class, Object::toString));
    }

    @Override
    public HandlerDescription describeHandlers() {
        return startedBy(String.class).usingMethod(this::startingSaga)
            .handleMessage(Integer.class).usingMethod(this::continueSaga)
            .finishDescription();
    }
}
