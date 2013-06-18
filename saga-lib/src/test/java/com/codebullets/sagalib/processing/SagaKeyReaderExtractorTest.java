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

import com.codebullets.sagalib.FunctionKeyReader;
import com.codebullets.sagalib.KeyReadFunction;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.Saga;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.inject.Provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SagaKeyReaderExtractor} class.
 */
public class SagaKeyReaderExtractorTest {
    private SagaKeyReaderExtractor sut;
    private Saga testSaga;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        testSaga = mock(Saga.class);

        SagaProviderFactory providerFactory = mock(SagaProviderFactory.class);
        Provider sagaProvider = mock(Provider.class);
        when(providerFactory.createProvider(Mockito.any(Class.class))).thenReturn(sagaProvider);
        when(sagaProvider.get()).thenReturn(testSaga);

        sut = new SagaKeyReaderExtractor(providerFactory);
    }

    /**
     * Given => saga has reader returning key.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader.
     */
    @Test
    public void findSagaInstanceKey_sagaHasReaderWithExactMessage_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader reader = FunctionKeyReader.create(String.class, new KeyReadFunction<String>() {
                @Override
                public String key(final String s) {
                    return keyValue;
                }
            });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader));

        // when
        String foundKey = sut.findSagaInstanceKey(Saga.class, "my input event message");

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo(keyValue));
    }

    /**
     * Given => Key reader is not part of saga readers list.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns null value.
     */
    @Test
    public void findSagaInstanceKey_readerForMessageNotPartOfSaga_returnsNull() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader reader = FunctionKeyReader.create(Integer.class, new KeyReadFunction<Integer>() {
            @Override
            public String key(Integer intVal) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader));

        // when
        String foundKey = sut.findSagaInstanceKey(Saga.class, "my input event message");

        // then
        assertThat("Expected returned key to be null.", foundKey, nullValue());
    }

    /**
     * Given => key extractor called first for same saga but different key.
     * When  => findSagaInstanceKey executed again.
     * Then  => Returns key from correct reader.
     */
    @Test
    public void findSagaInstanceKey_searchAlreadyDoneForOtherType_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader intReader = FunctionKeyReader.create(Integer.class, new KeyReadFunction<Integer>() {
            @Override
            public String key(final Integer intValue) {
                return "42";
            }
        });
        KeyReader reader = FunctionKeyReader.create(String.class, new KeyReadFunction<String>() {
            @Override
            public String key(final String s) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader, intReader));
        sut.findSagaInstanceKey(Saga.class, new Integer(5));

        // when
        String foundKey = sut.findSagaInstanceKey(Saga.class, "my input event message");

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo(keyValue));
    }


}