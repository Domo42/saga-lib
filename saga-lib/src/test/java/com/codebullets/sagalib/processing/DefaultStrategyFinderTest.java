/*
 * Copyright 2015 Stefan Domnanovits
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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.TestSaga;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.Timeout;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for {@link DefaultStrategyFinder} class.
 */
public class DefaultStrategyFinderTest {
    private DefaultStrategyFinder sut;
    private TypesForMessageMapper messageMapper;

    @Before
    public void init() {
        SagaInstanceFactory instanceFactory = mock(SagaInstanceFactory.class);
        KeyExtractor keyExtractor = mock(KeyExtractor.class);
        StateStorage storage = mock(StateStorage.class);
        messageMapper = mock(TypesForMessageMapper.class);

        sut = new DefaultStrategyFinder(messageMapper, instanceFactory, keyExtractor, storage);
    }

    /**
     * <pre>
     * Given => Timeout message parameter
     * When  => find is called
     * Then  => returns a timeout strategy
     * </pre>
     */
    @Test
    public void find_timeout_returnsTimeoutStrategy() {
        // given
        Timeout timeout = Timeout.create(null, null, null, null);
        LookupContext context = mockMessageContext(timeout);

        // when
        Collection<ResolveStrategy> strategies = sut.find(context);

        // then
        assertThat("Expected a timeout strategy.", strategies, hasItem(isA(TimeoutResolveStrategy.class)));
    }

    /**
     * <pre>
     * Given => message to create a new instance is called
     * When  => find is called
     * Then  => returns a start new saga strategy
     * </pre>
     */
    @Test
    public void find_newInstanceMessage_returnsStartNewSagaStrategy() {
        // given
        Object message = mockNewSagaMessage();
        LookupContext context = mockMessageContext(message);

        // when
        Collection<ResolveStrategy> strategies = sut.find(context);

        // then
        assertThat("Expected a new saga strategy.", strategies, hasItem(isA(StartNewSagaStrategy.class)));
    }

    /**
     * <pre>
     * Given => message to continue an existing saga
     * When  => find is called
     * Then  => returns a continue saga strategy
     * </pre>
     */
    @Test
    public void find_continueSagaMessage_returnsContinueSagaStrategy() {
        // given
        Object message = mockContinueMessage();
        LookupContext context = mockMessageContext(message);

        // when
        Collection<ResolveStrategy> strategies = sut.find(context);

        // then
        assertThat("Expected a continue all saga strategy.", strategies, hasItem(isA(ContinueAllStrategy.class)));
    }

    private Object mockNewSagaMessage() {
        Object message = new Object();
        SagaType sagaType = SagaType.startsNewSaga(TestSaga.class);

        when(messageMapper.getSagasForMessageType(message.getClass())).thenReturn(Lists.newArrayList(sagaType));
        return message;
    }

    private LookupContext mockMessageContext(final Object message) {
        LookupContext context = mock(LookupContext.class);
        when(context.message()).thenReturn(message);

        return context;
    }

    private Object mockContinueMessage() {
        Object message = new Object();
        SagaType sagaType = SagaType.continueSaga(TestSaga.class);

        when(messageMapper.getSagasForMessageType(message.getClass())).thenReturn(Lists.newArrayList(sagaType));
        return message;
    }
}