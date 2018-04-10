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

import com.codebullets.sagalib.context.LookupContext;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link StrategyInstanceResolver} class.
 */
public class StrategyInstanceResolverTest {
    private StrategyInstanceResolver sut;

    private StrategyFinder strategyFinder;

    @Before
    public void init() {
        strategyFinder = mock(StrategyFinder.class);
        sut = new StrategyInstanceResolver(strategyFinder);
    }

    /**
     * <pre>
     * Given => Finder returns a given strategy.
     * When  => resolve is called
     * Then  => call resolve on strategy
     * </pre>
     */
    @Test
    public void resolve_finderReturnsStrategy_callResolveOnStrategy() {
        // given
        LookupContext context = mock(LookupContext.class);
        ResolveStrategy strategy = mockStrategy(context);

        // when
        sut.resolve(context);

        // then
        verify(strategy).resolve(context);
    }

    /**
     * <pre>
     * Given => strategy returns an instance
     * When  => resolve is called
     * Then  => resolve return value contains instance
     * </pre>
     */
    @Test
    public void resolve_strategyReturnsInstance_instanceReturnedWithReturnValue() {
        // given
        ResolveStrategy strategy = mockStrategy();
        SagaInstanceInfo instanceInfo = new SagaInstanceInfo(null, true);
        when(strategy.resolve(any())).thenReturn(Lists.newArrayList(instanceInfo));

        // when
        Collection<SagaInstanceInfo> instances = sut.resolve(mock(LookupContext.class));

        // then
        assertThat("Expected instance in result set.", instances, hasItem(instanceInfo));
    }

    /**
     * <pre>
     * Given => strategy returns empty result set
     * When  => resolve is called
     * Then  => returns list is empty
     * </pre>
     */
    @Test
    public void resolve_strategyReturnsEmptyList_resolveReturnListIsEmpty() {
        // given
        ResolveStrategy strategy = mockStrategy();
        when(strategy.resolve(any(LookupContext.class))).thenReturn(Collections.<SagaInstanceInfo>emptyList());

        // when
        Collection<SagaInstanceInfo> instances = sut.resolve(null);

        // then
        assertThat("Expected an empty list.", instances, hasSize(0));
    }

    private ResolveStrategy mockStrategy() {
        ResolveStrategy strategy = mock(ResolveStrategy.class);
        when(strategyFinder.find(any(LookupContext.class))).thenReturn(Lists.newArrayList(strategy));
        return strategy;
    }

    private ResolveStrategy mockStrategy(final LookupContext context) {
        ResolveStrategy strategy = mock(ResolveStrategy.class);
        when(strategyFinder.find(context)).thenReturn(Lists.newArrayList(strategy));
        return strategy;
    }
}