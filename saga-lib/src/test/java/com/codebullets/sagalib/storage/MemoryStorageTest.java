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
package com.codebullets.sagalib.storage;

import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.TestSaga;
import com.codebullets.sagalib.TestSagaState;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

/**
 * Tests for {@link MemoryStorage class}.
 */
public class MemoryStorageTest {
    private MemoryStorage sut;

    @Before
    public void init() {
        sut = new MemoryStorage();
    }

    /**
     * Given => State has been saved.
     * When  => load is called.
     * Then  => Returns saved state.
     */
    @Test
    public void load_stateSaved_returnsSavedState() {
        // given
        TestSagaState state = buildState();
        sut.save(state);

        // when
        SagaState loadedState = sut.load(state.getSagaId());

        // then
        assertThat("Expected previously saved state as load result.", loadedState, sameInstance((SagaState)state));
    }

    /**
     * Given => Multiple states have been saved.
     * When  => load is called with specific id.
     * Then  => Returns state instance with matching id.
     */
    @Test
    public void load_multipleStatesSaved_returnsStateWithRequestedId() {
        // Given
        TestSagaState expectedState = buildState();
        TestSagaState otherState = buildState();
        sut.save(expectedState);
        sut.save(otherState);

        // when
        SagaState loadedState = sut.load(expectedState.getSagaId());

        // then
        assertThat("Expected state instance with requested id.", loadedState, sameInstance((SagaState)expectedState));
    }

    /**
     * Given => State has been saved and afterwards deleted.
     * When  => load is called with state id.
     * Then  => Returns a null value when loading.
     */
    @Test
    public void load_stateSavedAndDeleted_returnsNullInstance() {
        // given
        TestSagaState sagaState = buildState();
        sut.save(sagaState);
        sut.delete(sagaState.getSagaId());

        // when
        SagaState loadedState = sut.load(sagaState.getSagaId());

        // then
        assertThat("Expected deleted saga not to be found.", loadedState, nullValue());
    }

    /**
     * Given => States has been save with specific instance key and type.
     * When  => load is called with type and key.
     * Then  => Returns added state.
     */
    @Test
    public void loadByKey_stateWithKeySaved_returnsSavedKey() {
        // given
        SagaState expectedState = buildState();
        SagaState state = buildState();
        sut.save(expectedState);
        sut.save(state);

        // when
        Collection<SagaState> loadedStates = convertToCollection(sut.load(expectedState.getType(), expectedState.instanceKeys().iterator().next()));

        // then
        assertThat("Expected a single state entry.", loadedStates.size(), equalTo(1));
        assertThat("Expected state with given type and key.", loadedStates, hasItem(expectedState));
    }

    /**
     * Given => Multiple states with same instance key and type added.
     * When  => load is called with type and key.
     * Then  => Returns list with same key.
     */
    @Test
    public void loadByKey_multipleEntriesWithSameKeyAdded_returnsAllEntriesWithKey() {
        // given
        TestSagaState newState = addNewTestState(sut);
        addNewTestState(sut);
        TestSagaState other = buildState(newState.instanceKey());
        sut.save(other);

        // when
        // when
        Collection<SagaState> loadedStates = convertToCollection(sut.load(newState.getType(), newState.instanceKey()));

        // then
        assertThat("Expected a two state entries.", loadedStates, hasSize(2));
        assertThat("Expected state with given type and key.", loadedStates, hasItem(newState));
        assertThat("Expected state with given type and key.", loadedStates, hasItem(other));
    }

    /**
     * Given => Items is saved and then deleted.
     * When  => load is called with type and key.
     * Then  => Item no longer returned after deletion.
     */
    @Test
    public void loadByKey_itemSavedAndDelete_noLongerReturnsKey() {
        // given
        TestSagaState saga = addNewTestState(sut);
        sut.delete(saga.getSagaId());

        // when
        Collection<SagaState> foundState = convertToCollection(sut.load(saga.getType(), saga.instanceKey()));

        // then
        assertThat("Expected to find nothing after deletion.", foundState, hasSize(0));
    }

    /**
     * <pre>
     * Given => State saved with multiple instance keys.
     * When  => load is called with either key
     * Then  => Both calls return same saved instance.
     * </pre>
     */
    @Test
    public void loadByKey_itemSavedWithMultipleKey_returnsInstanceWithEitherKey() {
        // given
        String key1 = "key1_" + RandomStringUtils.randomAlphanumeric(10);
        String key2 = "key1_" + RandomStringUtils.randomAlphanumeric(10);
        SagaState state = buildState(key1, key2);
        sut.save(state);

        // when
        SagaState state1 = sut.load(state.getType(), key1).iterator().next();
        SagaState state2 = sut.load(state.getType(), key2).iterator().next();

        // then
        assertThat("Expected sate1 to be the same instance as the added state.", state1, sameInstance(state));
        assertThat("Expected sate2 to be the same instance as the added state.", state2, sameInstance(state));
    }

    private TestSagaState addNewTestState(final StateStorage storage) {
        TestSagaState newState = buildState();
        storage.save(newState);

        return newState;
    }

    private <T> Collection<T> convertToCollection(final Collection<? extends T> source) {
        Collection<T> collection = new ArrayList<>(source.size());

        for (T entry : source) {
            collection.add(entry);
        }

        return collection;
    }

    private TestSagaState buildState() {
        return buildState("InstanceKey_" + RandomStringUtils.randomAlphanumeric(10));
    }

    private TestSagaState buildState(String ... instanceKeys) {
        TestSagaState state = new TestSagaState();
        for (String key : instanceKeys) {
            state.addInstanceKey(key);
        }

        state.setSagaId("SagaId_" + RandomStringUtils.randomAlphanumeric(10));
        state.setType(TestSaga.class.getName());

        return state;
    }
}