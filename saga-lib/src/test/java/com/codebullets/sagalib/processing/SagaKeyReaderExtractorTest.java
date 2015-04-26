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

import com.codebullets.sagalib.*;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.context.LookupContext;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.Nullable;
import javax.inject.Provider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
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
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage("my input event message"));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object)keyValue));
    }

    /**
     * <pre>
     * Given => Lookup context provided for message
     * When  => instance for message is looked up
     * Then  => key reader gets original context as parameter
     * </pre>
     */
    @Test
    public void findSagaInstanceKey_sagaCalledWithContext_contextUsedAsReaderParameter() {
        // given
        LookupContext originalContext = SagaLookupContext.forMessage("my input event message");
        final LookupContext[] providedReaderContext = new LookupContext[1];

        KeyReader reader = KeyReaders.forMessage(String.class, new ContextKeyExtractFunction<String, String>() {
                @Nullable
                @Override
                public String key(final String message, final LookupContext context) {
                    providedReaderContext[0] = context;
                    return null;
                }
            });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader));

        // when
        sut.findSagaInstanceKey(Saga.class, originalContext);

        // then
        assertThat("Expected context in key reader to be the same as the original.", providedReaderContext[0], sameInstance(originalContext));
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
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage("my input event message"));

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
        sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage(new Integer(5)));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage("my input event message"));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object) keyValue));
    }

    /**
     * Given => saga has reader returning key that is superclass of the message.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader.
     */
    @Test
    public void findSagaInstanceKey_sagaHasReaderWithSuperclassOfMessage_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader reader = FunctionKeyReader.create(Number.class, new KeyReadFunction<Number>() {
            @Override
            public String key(final Number number) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage(Integer.valueOf(123)));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object)keyValue));
    }

    /**
     * Given => saga has both readers returning key that is superclass of the message and the message class.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader, preferring message class.
     */
    @Test
    public void findSagaInstanceKey_sagaHasBothReadesrWithSuperclassOfMessageAndMessageClass_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader numberReader = FunctionKeyReader.create(Number.class, new KeyReadFunction<Number>() {
            @Override
            public String key(final Number number) {
                return "some key";
            }
        });
        KeyReader integerReader = FunctionKeyReader.create(Integer.class, new KeyReadFunction<Integer>() {
            @Override
            public String key(final Integer integer) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(numberReader, integerReader));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage(Integer.valueOf(123)));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object)keyValue));
    }

    /**
     * Given => saga has reader returning key that is interface implemented by the message.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader.
     */
    @Test
    public void findSagaInstanceKey_sagaHasReaderWithInterfaceImplementedByMessage_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader reader = FunctionKeyReader.create(CharSequence.class, new KeyReadFunction<CharSequence>() {
            @Override
            public String key(final CharSequence charSequence) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage("my input event message"));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object) keyValue));
    }

    /**
     * Given => saga has both readers returning key that is interface implemented by the message and the message class.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader, preferring message class.
     */
    @Test
    public void findSagaInstanceKey_sagaHasBothReadesrWithInterfaceImplementedByMessageAndMessageClass_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader reader = FunctionKeyReader.create(CharSequence.class, new KeyReadFunction<CharSequence>() {
            @Override
            public String key(final CharSequence charSequence) {
                return "some key";
            }
        });
        KeyReader stringReader = FunctionKeyReader.create(String.class, new KeyReadFunction<String>() {
            @Override
            public String key(final String string) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(reader, stringReader));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage("my input event message"));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object) keyValue));
    }

    /**
     * Given => saga has all readers returning key that is interface implemented by message class, key that is superclass of the message and the message class.
     * When  => findSagaInstanceKey is executed.
     * Then  => Returns key from reader, preferring message class.
     */
    @Test
    public void findSagaInstanceKey_sagaHasBothReadersWithSuperclassOfMessageAndMessageClass_returnsDefinedKey() {
        // given
        final String keyValue = "theKeyValue";
        KeyReader comparableReader = FunctionKeyReader.create(Comparable.class, new KeyReadFunction<Comparable>() {
            @Override
            public String key(final Comparable number) {
                return "some key";
            }
        });
        KeyReader numberReader = FunctionKeyReader.create(Number.class, new KeyReadFunction<Number>() {
            @Override
            public String key(final Number number) {
                return "another key";
            }
        });
        KeyReader integerReader = FunctionKeyReader.create(Integer.class, new KeyReadFunction<Integer>() {
            @Override
            public String key(final Integer integer) {
                return keyValue;
            }
        });
        when(testSaga.keyReaders()).thenReturn(Lists.newArrayList(comparableReader, numberReader, integerReader));

        // when
        Object foundKey = sut.findSagaInstanceKey(Saga.class, SagaLookupContext.forMessage(Integer.valueOf(123)));

        // then
        assertThat("Expected returned key to match key value provided reader.", foundKey, equalTo((Object) keyValue));
    }
}