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
import com.codebullets.sagalib.TestSagaState;
import com.codebullets.sagalib.describe.DirectDescription;
import com.codebullets.sagalib.describe.SagaDescription;

import java.util.Collection;

import static com.codebullets.sagalib.describe.SagaDescriptions.startedBy;

public class DirectDescriptionSaga extends AbstractSaga<TestSagaState> implements DirectDescription {

    private void startingSaga(final String startingMessage) {
    }

    private void continueSaga(final Integer intMessage) {
    }

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return null;
    }

    @Override
    public SagaDescription describe() {
        return startedBy(String.class).usingMethod(this::startingSaga)
            .handleMessage(Integer.class).usingMethod(this::continueSaga)
            .finishDescription();
    }
}
