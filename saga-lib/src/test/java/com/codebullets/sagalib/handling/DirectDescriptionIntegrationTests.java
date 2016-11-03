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

package com.codebullets.sagalib.handling;

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DirectDescriptionIntegrationTests {
    private MessageStream messageStream;
    private Map<String, String> context;

    @Before
    public void initDirectDescriptionIntegrationTests() {
        context = new HashMap<>();

        messageStream = EventStreamBuilder.configure()
                .usingScanner(provideTypes())
                .usingSagaProviderFactory(providerFactory())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        if (messageStream != null) {
            messageStream.close();
        }
    }

    @Test
    public void handle_startHandlerDescribed_callsStartedHandler() throws InvocationTargetException, IllegalAccessException {
        // given
        final String theMessage = "theStartingMessage";

        // when
        messageStream.handle(theMessage);

        // then
        assertThat("Expected start handler to be called.", context.get(DirectDescriptionSaga.START_CALLED_KEY), equalTo("true"));
    }

    @Test
    public void handle_handlerDescribed_callsStartedThenContinueHandler() throws InvocationTargetException, IllegalAccessException {
        // given
        final String theMessage = "42";
        final Integer continueMessage = 42;

        // when
        messageStream.handle(theMessage);
        messageStream.handle(continueMessage);

        // then
        assertThat("Expected start handler to be called.", context.get(DirectDescriptionSaga.START_CALLED_KEY), equalTo("true"));
        assertThat("Expected continue handler to be called.", context.get(DirectDescriptionSaga.CONTINUE_CALLED_KEY), equalTo("true"));
    }

    private TypeScanner provideTypes() {
        return () -> ImmutableList.of(DirectDescriptionSaga.class);
    }

    private SagaProviderFactory providerFactory() {
        return new SagaProviderFactory() {
            @Override
            public <T extends Saga> Provider<T> createProvider(Class<T> sagaClass) {
                Provider<T> provider = null;
                if (sagaClass.equals(DirectDescriptionSaga.class)) {
                    provider = (Provider) () -> new DirectDescriptionSaga(context);
                }

                return provider;
            }
        };
    }
}
