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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

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
        TestSagaState newState = addNewTestState();
        addNewTestState();
        TestSagaState other = buildState(newState.instanceKeys());
        sut.save(other);

        // when
        Collection<SagaState> loadedStates = convertToCollection(sut.load(newState.getType(), newState.instanceKeys().iterator().next()));

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
    public void loadByKey_itemSavedAndDeleted_noLongerReturnsKey() {
        // given
        TestSagaState saga = addNewTestState();
        String instanceKey = saga.instanceKeys().iterator().next();
        sut.delete(saga.getSagaId());

        // when
        Collection<SagaState> foundState = convertToCollection(sut.load(saga.getType(), instanceKey));

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
        String key2 = "key2_" + RandomStringUtils.randomAlphanumeric(10);
        SagaState state = buildState(key1, key2);
        sut.save(state);

        // when
        SagaState state1 = sut.load(state.getType(), key1).iterator().next();
        SagaState state2 = sut.load(state.getType(), key2).iterator().next();

        // then
        assertThat("Expected sate1 to be the same instance as the added state.", state1, sameInstance(state));
        assertThat("Expected sate2 to be the same instance as the added state.", state2, sameInstance(state));
    }

    /**
     * <pre>
     * Given => state saved multiple times with different key sets
     * When  => load is called using removed key
     * Then  => call returns an empty list
     * </pre>
     */
    @Test
    public void loadByKey_keyHasBeenRemovedWithSecondSave_returnsEmptyList() {
        // given
        String key1 = "key1_" + RandomStringUtils.randomAlphanumeric(10);
        String key2 = "key2_" + RandomStringUtils.randomAlphanumeric(10);
        TestSagaState state = buildState(key1, key2);
        sut.save(state);

        state.removeInstanceKey(key1);
        sut.save(state);

        // when
        Collection<? extends SagaState> searchResult = sut.load(state.getType(), key1);

        // then
        assertThat("Expected a null instance if instance key has been removed.", searchResult, hasSize(0));
    }

    private TestSagaState addNewTestState() {
        TestSagaState newState = buildState();
        sut.save(newState);

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

    private TestSagaState buildState(Set<String> instanceKeys) {
        String[] keys = (String[]) Array.newInstance(String.class, instanceKeys.size());

        Iterator<String> iterator = instanceKeys.iterator();
        for (int i = 0; i < instanceKeys.size() && iterator.hasNext(); ++i) {
            keys[i] = iterator.next();
        }

        return buildState(keys);
    }
}