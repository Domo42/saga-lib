package com.codebullets.sagalib.startup;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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
}
