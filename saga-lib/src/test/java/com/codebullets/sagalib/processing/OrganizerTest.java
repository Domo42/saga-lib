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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.AbstractSingleEventSaga;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Organizer} class.
 */
public class OrganizerTest {
    private Organizer sut;
    private SagaAnalyzer analyzer;

    @Before
    public void initOrganizer() {
        analyzer = mock(SagaAnalyzer.class);

        when(analyzer.scanHandledMessageTypes()).thenReturn(availableSagaTypes());

        sut = new Organizer(analyzer, mock(KeyExtractor.class));
    }

    /**
     * <pre>
     * Given => SagaB class set as first in preferred order.
     * When  => sagaTypes for message called.
     * Then  => SagaB class is first in line.
     * </pre>
     */
    @Test
    public void sagaTypesForMessage_sagaBdefinedAsFirst_returnsSagaBEntryAsFirstElement() {
        // given
        sut.setPreferredOrder(createOrder(SagaB.class));

        // when
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage("A string message");

        // then
        SagaType first = Iterables.getFirst(sagaTypes, null);
        assertThat("First entry needs to be of type SagaB", (Class) first.getSagaClass(), equalTo((Class) SagaB.class));
        assertThat("Returned list must contain all 3 saga types", Iterables.size(sagaTypes), equalTo(3));
    }

    /**
     * <pre>
     * Given => SagaB class set as first in preferred order.
     * When  => sagaTypes for message called.
     * Then  => SagaB class is first in line.
     * </pre>
     */
    @Test
    public void sagaTypesForMessage_sagaCBeforeB_returnsSagaCThenBElements() {
        // given
        sut.setPreferredOrder(createOrder(SagaC.class, SagaB.class));

        // when
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage("A string message");

        // then
        SagaType first = Iterables.getFirst(sagaTypes, null);
        assertThat("First entry needs to be of type SagaC", (Class) first.getSagaClass(), equalTo((Class) SagaC.class));
        assertThat("Second entry needs to be of type SagaB", (Class) Iterables.get(sagaTypes, 1).getSagaClass(), equalTo((Class) SagaB.class));
        assertThat("Returned list must contain all 3 saga types", Iterables.size(sagaTypes), equalTo(3));
    }

    private Collection<Class<? extends Saga>> createOrder(Class<? extends Saga> first) {
        Collection<Class<? extends Saga>> collection = new ArrayList<>(1);
        collection.add(first);

        return collection;
    }

    private Collection<Class<? extends Saga>> createOrder(Class<? extends Saga> first, Class<? extends Saga> second) {
        Collection<Class<? extends Saga>> collection = new ArrayList<>(1);
        collection.add(first);
        collection.add(second);

        return collection;
    }

    private Map<Class<? extends Saga>, SagaHandlersMap> availableSagaTypes() {
        Map<Class<? extends Saga>, SagaHandlersMap> sagaTypes = new HashMap<>();

        sagaTypes.put(SagaA.class, createMap(SagaA.class));
        sagaTypes.put(SagaB.class, createMap(SagaB.class));
        sagaTypes.put(SagaC.class, createMap(SagaC.class));

        return sagaTypes;
    }

    private SagaHandlersMap createMap(Class<? extends Saga> clazz) {
        SagaHandlersMap map  = new SagaHandlersMap(clazz);
        map.add(new MessageHandler(String.class, null, true));

        return map;
    }

    public class SagaA extends AbstractSingleEventSaga {
    }

    public class SagaB extends AbstractSingleEventSaga {
    }

    public class SagaC extends AbstractSingleEventSaga {
    }
}