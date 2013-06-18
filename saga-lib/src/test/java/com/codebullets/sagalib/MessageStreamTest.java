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
package com.codebullets.sagalib;

import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.codebullets.sagalib.storage.MemoryStorage;
import com.codebullets.sagalib.storage.StateStorage;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Integration Tests for the saga lib message stream interface.
 */
public class MessageStreamTest {
    private MessageStream sut;
    private StateStorage storage;

    @Before
    public void init() {
        storage = new MemoryStorage();

        sut = EventStreamBuilder.configure()
                .usingStorage(storage)
                .usingScanner(new LocalScanner())
                .usingSagaProviderFactory(new TestSagaProviderFactory())
                .build();
    }

    /**
     * Given => string message as input.
     * When  => Message is handled by stream.
     * Then  => Saga is started.
     */
    @Test
    public void handle_stringMessage_messageIsHandledBySaga() throws InvocationTargetException, IllegalAccessException {
        // given
        String message = "myTestMessage_" + RandomStringUtils.randomAlphanumeric(10);

        // when
        sut.handle(message);

        // then
        Collection<SagaState> sagaState = convertToCollection(storage.load(TestSaga.class.getName(), String.valueOf(TestSaga.INSTANCE_KEY)));
        assertThat("Expected entry presenting the started saga state.", sagaState, hasSize(1));
    }

    /**
     * Given => First string and then integer message.
     * When  => Messages are handled by the stream.
     * Then  => Saga is completed and therefore no longer in storage.
     */
    @Test
    public void handle_stringAndIntegerMessage_sagaFinishedAndNoLongerInStateStorage() throws InvocationTargetException, IllegalAccessException {
        // given
        String msg1 = "myTestMessage_" + RandomStringUtils.randomAlphanumeric(10);
        Integer msg2 = TestSaga.INSTANCE_KEY;

        // when
        sut.handle(msg1);
        sut.handle(msg2);

        // then
        Collection<SagaState> sagaState = convertToCollection(storage.load(TestSaga.class.getName(), String.valueOf(TestSaga.INSTANCE_KEY)));
        assertThat("Expected no longer an entry for the saga state.", sagaState, hasSize(0));
    }

    private <T> Collection<T> convertToCollection(Collection<? extends T> source) {
        Collection<T> newCollection = new ArrayList<>(source.size());
        for (T entry : source) {
            newCollection.add(entry);
        }

        return newCollection;
    }

    private static class LocalScanner implements TypeScanner {

        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            Collection<Class<? extends Saga>> sagas = new ArrayList<>();
            sagas.add(TestSaga.class);

            return sagas;
        }
    }

    private static class TestSagaProviderFactory implements SagaProviderFactory {
        @Override
        public Provider<? extends Saga> createProvider(final Class sagaClass) {
            return new Provider<TestSaga>() {
                @Override
                public TestSaga get() {
                    return new TestSaga();
                }
            };
        }
    }
}