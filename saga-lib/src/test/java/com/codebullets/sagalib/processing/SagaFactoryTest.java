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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.TestSaga;
import com.codebullets.sagalib.TestSagaState;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.Timeout;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SagaFactory} class.
 */
public class SagaFactoryTest {
    private SagaFactory sut;
    private KeyExtractor keyExtractor;
    private StateStorage stateStorage;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        keyExtractor = mock(KeyExtractor.class);
        stateStorage = mock(StateStorage.class);

        SagaProviderFactory sagaProviderFactory = mock(SagaProviderFactory.class);
        Provider provider = new TestSagaProvider();
        when(sagaProviderFactory.createProvider(TestSaga.class)).thenReturn(provider);

        SagaAnalyzer sagaAnalyzer = mock(SagaAnalyzer.class);
        when(sagaAnalyzer.scanHandledMessageTypes()).thenReturn(createFakeTestSagaHandlersMap());

        sut = new SagaFactory(sagaAnalyzer, sagaProviderFactory, keyExtractor, stateStorage, mock(TimeoutManager.class));
    }

    /**
     * Given => Message to start a new saga.
     * When  => create is called.
     * Then  => Returns a new started saga.
     */
    @Test
    public void create_messageStartingTheSaga_returnsNewSagaInstance() {
        // given
        String message = "using string as input message";

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(message);

        // then
        assertThat("Expected only one saga to be created.", sagas, hasSize(1));

        SagaInstanceDescription instance = Iterables.get(sagas, 0);
        assertThat("Expected an instance of TestSaga.", instance.getSaga(), instanceOf(TestSaga.class));
    }

    /**
     * Given => Message to start a new saga.
     * When  => create is called.
     * Then  => Returns saga with saga state values initialized.
     */
    @Test
    public void create_messageStartingTheSaga_newSagaStateHasIdAndType() {
        // given
        String message = "using string as input message";

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(message);

        // then
        TestSaga saga = (TestSaga) Iterables.get(sagas, 0).getSaga();
        assertThat("Saga needs to have a saga state.", saga.state(), notNullValue());
        assertThat("Saga state needs a new id.", saga.state().getSagaId(), notNullValue());
        assertThat("Saga state needs type of originating saga.", saga.state().getType(), equalTo(TestSaga.class.getName()));
    }

    /**
     * Given => Message to start a new saga.
     * When  => create is called.
     * Then  => The saga description object returns true for new started saga.
     */
    @Test
    public void create_messageStartingTheSaga_descriptionDefinesStartingSagaTrue() {
        // given
        String message = "using string as input message";

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(message);

        // then
        SagaInstanceDescription description = Iterables.get(sagas, 0);
        assertThat("Expected starting info to be true.", description.isStarting(), equalTo(true));
    }

    /**
     * Given => Message to continue an existing saga.
     * When  => create is called.
     * Then  => The returned saga description indicates saga is not starting.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void create_messageContinuesSaga_descriptionDefinesSagaStartingFalse() {
        // given
        Integer message = 42;
        TestSagaState existingState = new TestSagaState();
        String instanceKey = "theInstanceKey";
        when(keyExtractor.findSagaInstanceKey(TestSaga.class, message)).thenReturn(instanceKey);
        when(stateStorage.load(TestSaga.class.getName(), instanceKey)).thenReturn((Collection)Lists.newArrayList(existingState));

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(message);

        // then
        SagaInstanceDescription description = Iterables.get(sagas, 0);
        assertThat("Saga description should indicate false for continued sagas.", description.isStarting(), equalTo(false));
    }

    /**
     * Given => Message to continue an existing saga.
     * When  => create is called.
     * Then  => Returns saga with existing saga state attached.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void create_messageContinuesSaga_assignsExistingStateToSagaInstance() {
        // given
        Integer message = 42;
        TestSagaState existingState = new TestSagaState();
        String instanceKey = "theInstanceKey";
        when(keyExtractor.findSagaInstanceKey(TestSaga.class, message)).thenReturn(instanceKey);
        when(stateStorage.load(TestSaga.class.getName(), instanceKey)).thenReturn((Collection) Lists.newArrayList(existingState));

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(message);

        // then
        assertThat("Expected a saga to be created.", sagas, hasSize(1));
        assertThat("Saga state assigned has to be the existing one.", Iterables.get(sagas, 0).getSaga().state(), sameInstance((SagaState) existingState));
    }

    /**
     * Given => Timeout message for saga.
     * When  => create is called
     * Then  => Returns existing saga based on timeout.
     */
    @Test
    public void create_timeoutMessage_returnsExistingSagaInstance() {
        // given
        Timeout timeout = Timeout.create("sagaId", "timeoutName", new Date());
        TestSagaState existingState = new TestSagaState();
        existingState.setType(TestSaga.class.getName());
        when(stateStorage.load(timeout.getSagaId())).thenReturn(existingState);

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(timeout);

        // then
        assertThat("Expected one saga entry for single timeout.", sagas, hasSize(1));
        assertThat("Returned saga has existing state attached.", Iterables.get(sagas, 0).getSaga().state(), sameInstance((SagaState) existingState));
    }

    /**
     * Given => Timeout message for saga.
     * When  => create is called
     * Then  => Return description indicates false for timeout.
     */
    @Test
    public void create_timeoutMessage_descriptionDefinesSagaStartingFalse() {
        // given
        Timeout timeout = Timeout.create("sagaId", "timeoutName", new Date());
        TestSagaState existingState = new TestSagaState();
        existingState.setType(TestSaga.class.getName());
        when(stateStorage.load(timeout.getSagaId())).thenReturn(existingState);

        // when
        Collection<SagaInstanceDescription> sagas = sut.create(timeout);

        // then
        SagaInstanceDescription description = Iterables.get(sagas, 0);
        assertThat("Expected started info to be false for timeouts.", description.isStarting(), equalTo(false));
    }

    private Map<Class<? extends Saga>, SagaHandlersMap> createFakeTestSagaHandlersMap() {
        SagaHandlersMap handlers = new SagaHandlersMap(TestSaga.class);
        handlers.add(new MessageHandler(String.class, null, true));
        handlers.add(new MessageHandler(Integer.class, null));

        Map<Class<? extends Saga>, SagaHandlersMap> map = new HashMap<>();
        map.put(TestSaga.class, handlers);

        return map;
    }

    private static class TestSagaProvider implements Provider<TestSaga> {

        @Override
        public TestSaga get() {
            return new TestSaga();
        }
    }
}