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

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.codebullets.sagalib.timeout.Timeout;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matches incoming messages returning the expected saga types in the
 * defined order.
 */
public class Organizer {
    private static final Logger LOG = LoggerFactory.getLogger(Organizer.class);

    private final KeyExtractor keyExtractor;

    private final SagaTypeCacheLoader cacheLoader;
    private final LoadingCache<Class, Collection<SagaType>> sagasForMessageType;

    /**
     * Generates a new instance of Organizer.
     */
    @Inject
    public Organizer(final SagaAnalyzer analyzer, final KeyExtractor keyExtractor) {
        this.keyExtractor = keyExtractor;

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
     * Returns the saga types to create in the excepted order to be executed.
     */
    public Iterable<SagaType> sagaTypesForMessage(final Object message) {
        Collection<SagaType> sagaTypes;

        if (message instanceof Timeout) {
            // timeout is special. Has only one specific saga state and
            // saga id is already known
            Timeout timeout = (Timeout) message;
            SagaType sagaType = SagaType.sagaFromTimeout(timeout.getSagaId());
            sagaTypes = new ArrayList<>(1);
            sagaTypes.add(sagaType);
        } else {
            sagaTypes = prepareSagaTypeList(message);
        }

        return sagaTypes;
    }

    private Collection<SagaType> prepareSagaTypeList(final Object message) {
        Collection<SagaType> sagasToExecute = sagasForMessageType.getUnchecked(message.getClass());
        Collection<SagaType> sagaTypes = new ArrayList<>(sagasToExecute.size());

        for (SagaType type : sagasToExecute) {
            if (type.isStartingNewSaga()) {
                sagaTypes.add(type);
            } else {
                // for continuation the instance key needs to be set.
                String key = readInstanceKey(type, message);
                if (key != null) {
                    sagaTypes.add(SagaType.continueSaga(type, key));
                } else {
                    LOG.error("Can not determine saga instance key from message {}", message);
                }
            }
        }

        return sagaTypes;
    }

    private String readInstanceKey(final SagaType sagaType, final Object message) {
        return keyExtractor.findSagaInstanceKey(sagaType.getSagaClass(), message);
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