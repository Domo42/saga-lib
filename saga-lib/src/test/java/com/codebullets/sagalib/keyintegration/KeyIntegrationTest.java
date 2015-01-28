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

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test whether saga lib works with different instance key than String
 */
public class KeyIntegrationTest {
    private static final Logger LOG = LoggerFactory.getLogger(KeyIntegrationTest.class);

    private MessageStream messageStream;
    private TestState state;

    @Before
    public void initTest() {
        state = new TestState();

        messageStream = EventStreamBuilder.configure().usingSagaProviderFactory(new TestSagaProvider(state)).usingScanner(new Scanner())
                            .build();
    }

    /**
     * <pre>
     * Given => Saga having custom key.
     * When  => saga is triggered by message and response.
     * Then  => response is handled by finding state using custom key
     * </pre>
     */
    @Test
    public void handleSaga_messageIsUuid_responseIsHandled() throws InvocationTargetException, IllegalAccessException {
        // given
        UUID sagaKey = UUID.randomUUID();

        // when
        messageStream.handle(sagaKey.toString());
        messageStream.handle(new ResponseMessage(sagaKey));

        // then
        assertThat("Expected response to be handled in original saga.", state.isResponseHandled(), equalTo(true));
    }

    @Test
    public void handleSaga_unknownMessage_doNotShowWarning() throws InvocationTargetException, IllegalAccessException {
        // given
        Integer unknownMessage = 42;

        // when
        messageStream.handle(unknownMessage);
    }

    @Test
    public void handleSaga_messageWithNoSagaState_doShowWarning() throws InvocationTargetException, IllegalAccessException {
        messageStream.handle(new ResponseMessage(UUID.randomUUID()));
    }

    private static class Scanner implements TypeScanner {

        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            ArrayList<Class<? extends Saga>> sagaTypes = new ArrayList<>();
            sagaTypes.add(StatefulSaga.class);
            sagaTypes.add(DeadMessageSaga.class);

            return sagaTypes;
        }
    }

    private static class TestSagaProvider implements SagaProviderFactory {
        private final TestState state;
        public TestSagaProvider(final TestState state) {
            this.state = state;
        }

        @Override
        public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
            Provider provider = null;

            if (sagaClass.equals(StatefulSaga.class)) {
                provider = new Provider<StatefulSaga>() {
                    @Override
                    public StatefulSaga get() {
                        return new StatefulSaga(state);
                    }
                };
            } else if (sagaClass.equals(DeadMessageSaga.class)) {
                provider = new Provider<DeadMessageSaga>() {
                    @Override
                    public DeadMessageSaga get() {
                        return new DeadMessageSaga();
                    }
                };
            }

            return provider;
        }
    }
}