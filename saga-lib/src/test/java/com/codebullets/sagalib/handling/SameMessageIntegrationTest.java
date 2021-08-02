/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.handling;

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SameMessageIntegrationTest {
    private AtomicInteger startCounter;
    private AtomicInteger continueCounter;

    private MessageStream messageStream;

    @BeforeEach
    void initSameMessageIntegrationTes() {
        startCounter = new AtomicInteger(0);
        continueCounter = new AtomicInteger(0);

        messageStream = EventStreamBuilder.configure()
                .usingScanner(provideTypes())
                .usingSagaProviderFactory(providerFactory())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (messageStream != null) {
            messageStream.close();
        }
    }

    @RepeatedTest(value = 100)
    void handle_startMessage_shallCallStartsSagaMethod() throws InvocationTargetException, IllegalAccessException {
        // given
        StartMessage theMessage = new StartMessage("instance key");

        // when
        messageStream.handle(theMessage);

        // then
        assertThat("Expected startsSaga to be called.", startCounter.get(), equalTo(1));
        assertThat("Expected continuesSaga not to be called.", continueCounter.get(), equalTo(0));
    }

    @RepeatedTest(value = 100)
    void handle_startMessageTwice_continuesExistingSagaAndStartsNewOne() throws InvocationTargetException, IllegalAccessException {
        // given
        StartMessage theMessage = new StartMessage("instance key");

        // when
        messageStream.handle(theMessage);
        messageStream.handle(theMessage);

        // then
        assertThat("Expected startsSaga to be called twice.", startCounter.get(), equalTo(2));
        assertThat("Expected continuesSaga to be called once.", continueCounter.get(), equalTo(1));
    }

    private TypeScanner provideTypes() {
        return () -> ImmutableList.of(SameMessageSaga.class);
    }

    private SagaProviderFactory providerFactory() {
        return new SagaProviderFactory() {
            @Override
            public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
                Provider<T> provider = null;
                if (sagaClass.equals(SameMessageSaga.class)) {
                    provider = (Provider) () -> new SameMessageSaga(startCounter, continueCounter);
                }

                return provider;
            }
        };
    }
}