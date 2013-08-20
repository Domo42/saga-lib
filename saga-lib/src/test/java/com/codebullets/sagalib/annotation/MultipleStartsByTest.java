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
package com.codebullets.sagalib.annotation;

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

/**
 * Tests whether multiple {@link com.codebullets.sagalib.StartsSaga} annotations work on
 * same saga class.
 */
public class MultipleStartsByTest {
    private MessageStream msgStream;
    private Set<String> recorder;

    @Before
    public void initTest() {
        recorder = new TreeSet<>();

        msgStream = EventStreamBuilder.configure()
                .usingScanner(new LocalScanner())
                .usingSagaProviderFactory(new SagaProvider(recorder))
                .build();
    }

    /**
     * <pre>
     * Given => String message as input.
     * When  => Message is added to stream.
     * Then  => Saga handles string message.
     * </pre>
     */
    @Test
    public void handle_stringMessage_stringMessageHasBeenHandled() throws InvocationTargetException, IllegalAccessException {
        // given
        String message = "anyString";

        // when
        msgStream.handle(message);

        // then
        assertThat("Expected string entry in recorder.", recorder, hasItem(message.getClass().getSimpleName()));
    }

    /**
     * <pre>
     * Given => Integer message as input.
     * When  => Message is added to stream.
     * Then  => Saga handles integer message.
     * </pre>
     */
    @Test
    public void handle_integerMessage_integerMessageHasBeenHandled() throws InvocationTargetException, IllegalAccessException {
        // given
        Integer message = 42;

        // when
        msgStream.handle(message);

        // then
        assertThat("Expected integer entry in recorder.", recorder, hasItem(message.getClass().getSimpleName()));
    }

    private static class LocalScanner implements TypeScanner {

        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            Collection<Class<? extends Saga>> sagas = new ArrayList<>();
            sagas.add(StartedBySeveralTypesSaga.class);

            return sagas;
        }
    }

    private static class SagaProvider implements SagaProviderFactory {
        private final Set<String> recorder;
        public SagaProvider(final Set<String> recorder) {
            this.recorder = recorder;
        }

        @Override
        public Provider<? extends Saga> createProvider(final Class sagaClass) {
            return new Provider<Saga>() {
                    @Override
                    public Saga get() {
                        return new StartedBySeveralTypesSaga(recorder);
                    }
                };
        }
    }
}