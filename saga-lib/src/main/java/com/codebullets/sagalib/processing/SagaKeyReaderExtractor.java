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

import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.Saga;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Uses the saga keyReaders() method to extract the saga
 * key of a message.
 */
@SuppressWarnings("unchecked")
public class SagaKeyReaderExtractor implements KeyExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(SagaKeyReaderExtractor.class);
    private final SagaProviderFactory sagaProviderFactory;
    private final Cache<SagaMessageKey, KeyReader> knownReaders;

    /**
     * Generates a new instance of SagaKeyReaderExtractor.
     */
    @Inject
    public SagaKeyReaderExtractor(final SagaProviderFactory sagaProviderFactory) {
        this.sagaProviderFactory = sagaProviderFactory;
        knownReaders = CacheBuilder.newBuilder().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object findSagaInstanceKey(final Class<? extends Saga> sagaClazz, final LookupContext context) {
        Object keyValue = null;

        KeyReader reader = tryGetKeyReader(sagaClazz, context.message());
        if (reader != null) {
            keyValue = reader.readKey(context.message(), context);
        }

        return keyValue;
    }

    /**
     * Does not throw an exception when accessing the loading cache for key readers.
     */
    private KeyReader tryGetKeyReader(final Class<? extends Saga> sagaClazz, final Object message) {
        KeyReader reader;

        try {
            reader = knownReaders.get(
                    SagaMessageKey.forMessage(sagaClazz, message),
                    new Callable<KeyReader>() {
                        @Override
                        public KeyReader call() throws Exception {
                            return findReader(sagaClazz, message);
                        }
                    });
        } catch (Exception ex) {
            LOG.error("Error searching for reader to extract saga key. sagatype = {}, message = {}", sagaClazz, message, ex);
            reader = null;
        }

        return reader;
    }

    private KeyReader findReader(final Class<? extends Saga> sagaClazz, final Object message) {
        KeyReader reader = null;

        Collection<KeyReader> readersOfSaga = findReader(sagaClazz, message.getClass());

        ClassTypeExtractor extractor = new ClassTypeExtractor(message.getClass());
        Iterable<Class<?>> messageTypesToConsider = extractor.allClassesAndInterfaces();

        for (Class<?> messageType : messageTypesToConsider) {
            reader = findReaderMatchingExactType(readersOfSaga, messageType);
            if (reader != null) {
                break;
            }
        }

        return reader;
    }

    /**
     * Search for reader based on message class.
     */
    private KeyReader findReaderMatchingExactType(final Iterable<KeyReader> readers, final Class<?> messageType) {
        KeyReader messageKeyReader = null;

        for (KeyReader reader : readers) {
            if (reader.getMessageClass().equals(messageType)) {
                messageKeyReader = reader;
                break;
            }
        }

        return messageKeyReader;
    }

    /**
     * Search for a reader based on saga type.
     */
    private Collection<KeyReader> findReader(final Class<? extends Saga> sagaClazz, final Class<?> messageClazz) {
        Saga saga = sagaProviderFactory.createProvider(sagaClazz).get();
        Collection<KeyReader> readers = saga.keyReaders();
        if (readers == null) {
            // return empty list in case saga returns null for any reason
            readers = new ArrayList<>();
        }

        return readers;
    }

    /**
     * Key combined of saga and message type.
     */
    private static final class SagaMessageKey {
        private final Class<?> saga;
        private final Class<?> message;

        private SagaMessageKey(final Class<?> saga, final Class<?> message) {
            this.saga = saga;
            this.message = message;
        }

        @Override
        public int hashCode() {
            return Objects.hash(saga, message);
        }

        @Override
        public boolean equals(final Object obj) {
            boolean equals = false;
            if (this == obj) {
                equals = true;
            } else if (obj instanceof SagaMessageKey) {
                SagaMessageKey other = (SagaMessageKey) obj;
                equals = Objects.equals(other.saga, saga) && Objects.equals(other.message, message);
            }

            return equals;
        }

        public static SagaMessageKey forMessage(final Class<?> saga, final Object message) {
            checkNotNull(saga, "saga type must not be null.");

            return new SagaMessageKey(saga, message.getClass());
        }
    }
}