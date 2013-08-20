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
package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.TestSaga;
import org.junit.Test;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ReflectionsTypeScanner} class.
 */
@SuppressWarnings("unchecked")
public class ReflectionsTypeScannerTest {
    /**
     * Given => Scanner is created with default ctor.
     * When  => sanForSagas is called.
     * Then  => Returns TestSaga from class path.
     */
    @Test
    public void scanForSagas_defaultCtor_returnsTestSaga() {
        // given
        ReflectionsTypeScanner sut = new ReflectionsTypeScanner();

        // when
        Collection<Class<? extends Saga>> foundSagas = sut.scanForSagas();

        // then
        assertThat("Expected TestSaga from class path to be found.", foundSagas, hasItem((Class<? extends Saga>) TestSaga.class));
    }

    /**
     * Given => Scanner is created with default ctor.
     * When  => scanForSagas is called.
     * Then  => Does not return abstract base class of saga.
     */
    @Test
    public void scanForSagas_defaultCtor_doesNotReturnAbstractBaseClass() {
        // given
        ReflectionsTypeScanner sut = new ReflectionsTypeScanner();

        // when
        Collection<Class<? extends Saga>> foundSagas = sut.scanForSagas();

        // then
        assertThat("Expected not to find AbstractSaga in returned list.", foundSagas, not(hasItem(AbstractSaga.class)));
    }

    /**
     * Given => Scanner is created with custom reflections scanner.
     * When  => scanForSagas is called.
     * Then  => Returns empty list as mock does not return values.
     */
    @Test
    public void scanForSagas_reflectionsCtor_returnsEmptyListFromReflectionsParam() {
        // given
        Reflections reflections = mock(Reflections.class);
        when(reflections.getSubTypesOf(any(Class.class))).thenReturn(new HashSet<>());
        ReflectionsTypeScanner sut = new ReflectionsTypeScanner(reflections);

        // when
        Collection<Class<? extends Saga>> foundSagas = sut.scanForSagas();

        // then
        assertThat("Expected return value to be an empty list.", foundSagas.size(), equalTo(0));
    }

    private <T> Collection<T> convertToCollection(Collection<? extends T> source) {
        Collection<T> newCollection = new ArrayList<>(source.size());
        for (T entry : source) {
            newCollection.add(entry);
        }

        return newCollection;
    }
}