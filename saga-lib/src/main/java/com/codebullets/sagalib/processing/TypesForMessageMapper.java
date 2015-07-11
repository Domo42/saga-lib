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

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Knows about the types of sagas to be created based on the message.
 * This is done by scanning the the sagas for annotations.<br>
 * Based on the parameter types of the annotated handlers a list of specific saga types
 * is created per incoming message.
 *
 * <p>The returned order is important, and can be manipulated by setting using
 * {@link #setPreferredOrder(Collection)} to manipulate the result.</p>
 */
public class TypesForMessageMapper {
    private final SagaTypeCacheLoader cacheLoader;
    private final LoadingCache<Class, Collection<SagaType>> sagasForMessageType;

    /**
     * Generates a new instance of TypesForMessageMapper.
     */
    @Inject
    public TypesForMessageMapper(final SagaAnalyzer analyzer) {
        // scan for sagas and their messages being handled
        Map<Class<? extends Saga>, SagaHandlersMap> handlersMap = analyzer.scanHandledMessageTypes();
        cacheLoader = new SagaTypeCacheLoader(initializeMessageMappings(handlersMap));
        sagasForMessageType = CacheBuilder.newBuilder().build(cacheLoader);
    }

    /**
     * Sets the handlers that should be executed first.
     */
    public void setPreferredOrder(final Collection<Class<? extends Saga>> preferredOrder) {
        checkNotNull(preferredOrder, "Preferred order list may not be null. Empty is allowed.");

        cacheLoader.setPreferredOrder(preferredOrder);
        sagasForMessageType.invalidateAll();
    }

    /**
     * Returns a list of saga types that have an annotated handler method matching the provided message class.
     */
    public Collection<SagaType> getSagasForMessageType(final Class messageClass) {
        return sagasForMessageType.getUnchecked(messageClass);
    }

    /**
     * Populate internal map to translate between incoming message event type and saga type.
     */
    private Multimap<Class, SagaType> initializeMessageMappings(final Map<Class<? extends Saga>, SagaHandlersMap> handlersMap) {
        Multimap<Class, SagaType> scannedTypes = LinkedListMultimap.create();

        for (Map.Entry<Class<? extends Saga>, SagaHandlersMap> entry : handlersMap.entrySet()) {
            Class<? extends Saga> sagaClass = entry.getKey();

            Collection<MessageHandler> sagaHandlers = entry.getValue().messageHandlers();
            for (MessageHandler handler : sagaHandlers) {

                // remember all message types where a completely new saga needs to be started.
                if (handler.getStartsSaga()) {
                    scannedTypes.put(handler.getMessageType(), SagaType.startsNewSaga(sagaClass));
                } else {
                    scannedTypes.put(handler.getMessageType(), SagaType.continueSaga(sagaClass));
                }
            }
        }

        return scannedTypes;
    }
}