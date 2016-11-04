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

package com.codebullets.sagalib.annotations;

import com.codebullets.sagalib.*;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

public class AnnotationSaga extends AbstractSaga<AnnotationSagaState> {

    @StartsSaga
    public void startSaga(final AnnotationStartingMessage startingMessage) {
        state().addInstanceKey(startingMessage.key());
    }

    @EventHandler
    public void finishSaga(final AnnotationFinishMessage finishMessage) {
        setFinished();
    }

    @Override
    public void createNewState() {
        setState(new AnnotationSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return ImmutableSet.of(KeyReaders.forMessage(AnnotationFinishMessage.class, AnnotationFinishMessage::key));
    }
}
