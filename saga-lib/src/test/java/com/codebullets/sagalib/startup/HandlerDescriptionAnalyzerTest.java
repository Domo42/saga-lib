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
package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.AbstractHandler;
import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.describe.DescribesHandlers;
import com.codebullets.sagalib.describe.HandlerDescription;
import com.codebullets.sagalib.describe.HandlerDescriptions;
import com.codebullets.sagalib.describe.HandlerTypeDefinition;
import com.codebullets.sagalib.processing.SagaInstanceCreator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HandlerDescriptionAnalyzerTest {
    private HandlerDescriptionAnalyzer sut;
    private TypeScanner typeScanner;
    private SagaInstanceCreator instanceCreator;

    @Before
    public void initDirectDescriptionAnalyzerTest() {
        typeScanner = mock(TypeScanner.class);
        instanceCreator = mock(SagaInstanceCreator.class);

        sut = new HandlerDescriptionAnalyzer(typeScanner, instanceCreator);
    }

    @Test
    public void scanHandledMessageTypes_sagaWithHandler_returnsHandler() throws ExecutionException {
        // given
        mockSaga(String.class);

        // when
        Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap = sut.scanHandledMessageTypes();

        // then
        assertThat("Expected saga type to be present.", classSagaHandlersMapMap.containsKey(TestSaga.class), is(true));

        Collection<MessageHandler> handlers = classSagaHandlersMapMap.get(TestSaga.class).messageHandlers();
        assertThat("Expected handlers map to contain handler type.", handlers.iterator().next().getMessageType(), equalTo(String.class));
        assertThat("Expected handlers map to to be starting type.", handlers.iterator().next().getStartsSaga(), is(true));
    }

    @Test
    public void scanHandledMessageTypes_sagaWithContinueHandler_returnsHandler() throws ExecutionException {
        // given
        mockSaga(String.class, Integer.class);

        // when
        Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap = sut.scanHandledMessageTypes();

        // then
        assertThat("Expected saga type to be present.", classSagaHandlersMapMap.containsKey(TestSaga.class), is(true));

        Collection<MessageHandler> handlers = classSagaHandlersMapMap.get(TestSaga.class).messageHandlers();
        Optional<MessageHandler> continueHandler = handlers.stream().filter(h -> h.getMessageType().equals(Integer.class)).findFirst();
        assertThat("Expected handlers map to contain handler type", continueHandler.isPresent(), is(true));
        assertThat("Expected handlers map to to be starting type.", continueHandler.get().getStartsSaga(), is(false));
    }

    @Test
    public void scanHandledMessageTypes_sagaNotImplementingDescription_returnsEmpty() throws ExecutionException {
        // given
        when(typeScanner.scanForSagas()).thenReturn(Collections.singleton(com.codebullets.sagalib.TestSaga.class));

        // when
        Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap = sut.scanHandledMessageTypes();

        // then
        assertThat("Expected map to be empty.", classSagaHandlersMapMap.isEmpty(), is(true));
    }

    @Test
    public void scanHandledMessageTypes_sagaIsAbstract_returnsEmpty() throws ExecutionException {
        // given
        when(typeScanner.scanForSagas()).thenReturn(Collections.singleton(AbstractHandler.class));

        // when
        Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap = sut.scanHandledMessageTypes();

        // then
        assertThat("Expected map to be empty.", classSagaHandlersMapMap.isEmpty(), is(true));
    }

    private Saga mockSaga(final Class<?> ... handledTypes) throws ExecutionException {
        TestSaga saga = new TestSaga(handledTypes);
        when(instanceCreator.createNew(TestSaga.class)).thenReturn(saga);
        when(typeScanner.scanForSagas()).thenReturn(Collections.singleton(TestSaga.class));
        return saga;
    }

    private static class TestSaga extends AbstractSaga implements DescribesHandlers {
        private Class<?>[] handledTypes;

        TestSaga(final Class<?> ... handledTypes) {
            this.handledTypes = handledTypes;
        }

        @Override
        public void createNewState() {
        }

        @Override
        public Collection<KeyReader> keyReaders() {
            return Collections.emptyList();
        }

        @Override
        public HandlerDescription describeHandlers() {
            HandlerTypeDefinition handlerTypeDefinition = HandlerDescriptions.startedBy(handledTypes[0]).usingMethod(msg -> { });
            for (int i = 1; i < handledTypes.length; ++i) {
                handlerTypeDefinition = handlerTypeDefinition.handleMessage(handledTypes[i]).usingMethod(msg -> { });
            }

            return handlerTypeDefinition.finishDescription();
        }
    }
}