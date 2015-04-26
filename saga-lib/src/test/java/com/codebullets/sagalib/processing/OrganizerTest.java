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
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.google.common.collect.Iterables;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Organizer} class.
 */
public class OrganizerTest {
    private Organizer sut;
    private SagaAnalyzer analyzer;
    private KeyExtractor keyExtractor;

    @Before
    public void initOrganizer() {
        analyzer = mock(SagaAnalyzer.class);
        keyExtractor = mock(KeyExtractor.class);

        when(analyzer.scanHandledMessageTypes()).thenReturn(availableSagaTypes());

        sut = new Organizer(analyzer, keyExtractor);
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
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage(SagaLookupContext.forMessage("A string message"));

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
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage(SagaLookupContext.forMessage("A string message"));

        // then
        SagaType first = Iterables.getFirst(sagaTypes, null);
        assertThat("First entry needs to be of type SagaC", first, matchesSagaClass(SagaC.class));
        assertThat("Second entry needs to be of type SagaB", (Class) Iterables.get(sagaTypes, 1).getSagaClass(), equalTo((Class) SagaB.class));
        assertThat("Returned list must contain all 3 saga types", Iterables.size(sagaTypes), equalTo(3));
    }

    /**
     * <pre>
     * Given => Integer as saga message.
     * When  => sagaTypeForMessage called.
     * Then  => Returns saga for concrete message and base class.
     * </pre>
     */
    @Test
    public void sagaTypesForMessage_integerMessage_returnsSagasForIntegerAndNumber() {
        // given
        Integer message = 42;

        // when
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage(SagaLookupContext.forMessage(message));

        assertThat("Expect list of 2 sagas", Iterables.size(sagaTypes), equalTo(2));
        assertThat("Contains integer saga.", sagaTypes, hasItem(matchesSagaClass(IntegerSaga.class)));
        assertThat("Contains number saga.", sagaTypes, hasItem(matchesSagaClass(NumberSaga.class)));
    }

    /**
     * <pre>
     * Given => List message type.
     * When  => sagaTypesForMessage called.
     * Then  => Returns saga matching not the direct but the base interface.
     * </pre>
     */
    @Test
    public void sagaTypesForMessage_listMessage_returnsSagaMatchingBaseInterface() {
        // given
        List message = new ArrayList();

        // when
        Iterable<SagaType> sagaTypes = sut.sagaTypesForMessage(SagaLookupContext.forMessage(message));

        // then
        assertThat("Expected a single return value.", Iterables.size(sagaTypes), equalTo(1));
        assertThat("Expected saga matching base interface handler.", sagaTypes, hasItem(matchesSagaClass(IterablesSaga.class)));
    }

    /**
     * <pre>
     * Given => Message is type to continue saga
     * When  => sagaTypesForMessage is called
     * Then  => Extracts instance key for message
     * </pre>
     */
    @Test
    public void sagaTypesForMessage_handlerMessage_extractInstanceKeyForMessage() {
        // given
        URI message = URI.create("mailto:any@anywhere.com");
        LookupContext context = SagaLookupContext.forMessage(message);

        // when
        sut.sagaTypesForMessage(context);

        // then
        verify(keyExtractor).findSagaInstanceKey(SagaD.class, context);
    }

    private Matcher<SagaType> matchesSagaClass(final Class<?> sagaClass) {
        return new BaseMatcher<SagaType>() {
                @Override
                public boolean matches(final Object o) {
                    SagaType sagaType = (SagaType) o;
                    return sagaType.getSagaClass().equals(sagaClass);
                }

                @Override
                public void describeTo(final Description description) {
                    description.appendText("SagaType matching saga class " + sagaClass.getSimpleName());
                }

            @Override
            public void describeMismatch(final Object item, final Description description) {
                String itemType = "<null>";
                if (item != null) {
                    itemType = (item instanceof SagaType) ? ((SagaType) item).getSagaClass().getSimpleName() : item.getClass().getSimpleName();
                }

                description.appendText("Expected SagaType with saga class " + sagaClass.getSimpleName() + " but was " + itemType);
            }
        };
    }

    private Collection<Class<? extends Saga>> createOrder(Class <? extends Saga> first) {
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
        sagaTypes.put(SagaD.class, createMap(SagaD.class, InetSocketAddress.class, URI.class));
        sagaTypes.put(IntegerSaga.class, createMap(IntegerSaga.class, Integer.class));
        sagaTypes.put(NumberSaga.class, createMap(NumberSaga.class, Number.class));
        sagaTypes.put(IterablesSaga.class, createMap(IterablesSaga.class, Iterable.class));

        return sagaTypes;
    }

    private SagaHandlersMap createMap(Class<? extends Saga> clazz, Class<?> startType) {
        SagaHandlersMap map = new SagaHandlersMap(clazz);
        map.add(new MessageHandler(startType, null, true));

        return map;
    }

    private SagaHandlersMap createMap(Class<? extends Saga> clazz, Class<?> startType, Class<?> handlerType) {
        SagaHandlersMap map = new SagaHandlersMap(clazz);
        map.add(new MessageHandler(startType, null, true));
        map.add(new MessageHandler(handlerType, null, false));

        return map;
    }

    private SagaHandlersMap createMap(Class<? extends Saga> clazz) {
        return createMap(clazz, String.class);
    }

    public class SagaA extends AbstractSingleEventSaga { }

    public class SagaB extends AbstractSingleEventSaga { }

    public class SagaC extends AbstractSingleEventSaga { }

    public class SagaD extends AbstractSingleEventSaga {}

    public class NumberSaga extends AbstractSingleEventSaga { }

    public class IntegerSaga extends AbstractSingleEventSaga { }

    public class IterablesSaga extends AbstractSingleEventSaga { }
}