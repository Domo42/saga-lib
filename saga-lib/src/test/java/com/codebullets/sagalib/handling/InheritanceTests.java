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
package com.codebullets.sagalib.handling;

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
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests to check whether saga-lib handler are executed for the
 * whole available type hierarchy of a message.
 *
 */
@SuppressWarnings("unchecked")
public class InheritanceTests {
    private MessageStream sagaLib;
    private MarkerInterfaceHandler markerHandler;

    @Before
    public void init() {
        markerHandler = new MarkerInterfaceHandler();

        sagaLib = EventStreamBuilder.configure()
                    .usingScanner(new TestsScanner())
                    .usingSagaProviderFactory(new SagaFactory(markerHandler))
                    .build();
    }

    /**
     * <pre>
     * Given => A concrete message to be handled
     * When  => message handler is called
     * Then  => Calls handler method for marker interface on base class.
     * </pre>
     */
    @Test
    public void handle_concreteMessage_handlerForInterfaceOnBaseClassIsCalled() throws InvocationTargetException, IllegalAccessException {
        // given
        ConcreteMessage message = new ConcreteMessage();

        // when
        sagaLib.handle(message);

        // then
        assertThat("Expected handler with marker interface to be called.", markerHandler.getHandlerCalled(), equalTo(true));
    }

    private static class TestsScanner implements TypeScanner {

        @Override
        public Collection<Class<? extends Saga>> scanForSagas() {
            List<Class<? extends Saga>> sagaTypesList = new ArrayList<>();
            sagaTypesList.add(MarkerInterfaceHandler.class);

            return sagaTypesList;
        }
    }

    private static class SagaFactory implements SagaProviderFactory {
        private final MarkerInterfaceHandler handler;

        public SagaFactory(MarkerInterfaceHandler handler) {
            this.handler = handler;
        }

        @Override
        public Provider<? extends Saga> createProvider(final Class sagaClass) {
            Provider provider = null;

            if (sagaClass.equals(MarkerInterfaceHandler.class)) {
                provider = new Provider<MarkerInterfaceHandler>() {
                        @Override
                        public MarkerInterfaceHandler get() {
                            return handler;
                        }
                    };
            }

            return provider;
        }
    }
}