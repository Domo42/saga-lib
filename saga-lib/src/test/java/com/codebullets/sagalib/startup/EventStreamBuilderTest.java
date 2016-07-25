package com.codebullets.sagalib.startup;


import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.TimeoutManager;
import org.junit.Test;

import javax.inject.Provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for {@link EventStreamBuilder} class.
 */
public class EventStreamBuilderTest {
    @Test
    public void configure_always_returnsStreamBuilderInstance() {
        // given

        // when
        StreamBuilder streamBuilder = EventStreamBuilder.configure();

        // then
        assertThat("Expected valid stream builder instance.", streamBuilder, not(nullValue()));
    }

    /**
     * Given => The provided timeout manager implements {@link AutoCloseable}.
     * When  => close is called.
     * Then  => close has been called on timeout manager.
     */
    @Test
    public void close_timeOutManagerIsCloseable_callsClose() throws Exception {
        // given
        TimeoutManager manager = mock(TimeoutManager.class, withSettings().extraInterfaces(AutoCloseable.class));
        StreamBuilder streamBuilder = EventStreamBuilder.configure().usingTimeoutManager(manager).usingSagaProviderFactory(new DummyProvider());
        streamBuilder.build();

        // when
        streamBuilder.close();

        // then
        AutoCloseable closeable = (AutoCloseable) manager;
        verify(closeable).close();
    }

    /**
     * Given => The provided state storage implements {@link AutoCloseable}.
     * When  => close is called.
     * Then  => close has been called on timeout manager.
     */
    @Test
    public void close_storageIsCloseable_callsClose() throws Exception {
        // given
        StateStorage storage = mock(StateStorage.class, withSettings().extraInterfaces(AutoCloseable.class));
        StreamBuilder streamBuilder = EventStreamBuilder.configure().usingStorage(storage).usingSagaProviderFactory(new DummyProvider());
        streamBuilder.build();

        // when
        streamBuilder.close();

        // then
        AutoCloseable closeable = (AutoCloseable) storage;
        verify(closeable).close();
    }

    private static class DummyProvider implements SagaProviderFactory {
        @Override
        public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
            return null;
        }
    }
}
