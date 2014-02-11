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
import com.codebullets.sagalib.Saga;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Uses the saga keyReaders() method to extract the saga
 * key of a message.
 */
@SuppressWarnings("unchecked")
public class SagaKeyReaderExtractor implements KeyExtractor {
    private static final Logger LOG = LoggerFactory.getLogger(SagaKeyReaderExtractor.class);
    private final SagaProviderFactory sagaProviderFactory;
    private final Cache<Class, Collection<KeyReader>> knownReaders;

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
    public String findSagaInstanceKey(final Class<? extends Saga> sagaClazz, final Object message) {
        String keyValue = null;

        Collection<KeyReader> sagaReaders = tryGetReaders(sagaClazz, message);
        KeyReader reader = findReader(sagaReaders, message);
        if (reader != null) {
            keyValue = reader.readKey(message);
        }

        return keyValue;
    }

    /**
     * Does not throw an exception when accessing the loading cache for key readers.
     */
    private Collection<KeyReader> tryGetReaders(final Class<? extends Saga> sagaClazz, final Object message) {
        Collection<KeyReader> readers;

        try {
            readers = knownReaders.get(sagaClazz, new Callable<Collection<KeyReader>>() {
                    @Override
                    public Collection<KeyReader> call() throws Exception {
                        return findReader(sagaClazz, message.getClass());
                    }
                });
        } catch (Exception ex) {
            LOG.error("Error searching for reader to extract saga key. sagatype = {}, message = {}", sagaClazz, message, ex);
            readers = new ArrayList<>();
        }

        return readers;
    }

    /**
     * Search for reader based on message class.
     */
    private KeyReader findReader(final Collection<KeyReader> sagaKeyReaders, final Object message) {
        KeyReader messageKeyReader = null;
        Class messageClass = message.getClass();

        for (KeyReader reader : sagaKeyReaders) {
            if (reader.getMessageClass().equals(messageClass)) {
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
}