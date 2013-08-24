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
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Matches incoming messages returning the expected saga types in the
 * defined order.
 */
public class Organizer {
    private static final Logger LOG = LoggerFactory.getLogger(Organizer.class);

    private final KeyExtractor keyExtractor;

    // the multi maps are read only once initialized in the ctor and therefore do not need thread synchronisation.
    private final Multimap<Class, Class<? extends Saga>> messagesToContinueSaga = HashMultimap.create();
    private final Multimap<Class, Class<? extends Saga>> messagesStartingSagas = HashMultimap.create();

    /**
     * Generates a new instance of Organizer.
     */
    @Inject
    public Organizer(final SagaAnalyzer analyzer, final KeyExtractor keyExtractor) {
        this.keyExtractor = keyExtractor;

        // scan for sagas and their messages being handled
        Map<Class<? extends Saga>, SagaHandlersMap> handlersMap = analyzer.scanHandledMessageTypes();
        initializeMessageMappings(handlersMap);
    }

    /**
     * Returns the saga types to create in the excepted order to be executed.
     */
    public Iterable<SagaType> sagaTypesForMessage(final Object message) {
        Collection<SagaType> sagaTypes = new ArrayList<>();

        if (message instanceof Timeout) {
            // timeout is special. Has only one specific saga state and
            // saga id is already known
            Timeout timeout = (Timeout) message;
            SagaType sagaType = SagaType.sagaFromTimeout(timeout.getSagaId());
            sagaTypes.add(sagaType);
        } else {
            sagaTypes.addAll(getSagasBeingStarted(message));
            sagaTypes.addAll(getSagasBeingContinued(message));
        }

        return sagaTypes;
    }

    /**
     * Creates a list of saga types continuing an existing saga.
     */
    private Collection<SagaType> getSagasBeingContinued(final Object message) {
        Collection<SagaType> sagaTypes = new ArrayList<>();

        Collection<Class<? extends Saga>> existingSagaTypes = messagesToContinueSaga.get(message.getClass());
        for (Class<? extends Saga> sagaType : existingSagaTypes) {

            String key = keyExtractor.findSagaInstanceKey(sagaType, message);
            if (key != null) {
                sagaTypes.add(SagaType.continueSaga(sagaType, key));
            } else {
                LOG.error("Can not determine saga instance key from message {}", message);
            }
        }

        return sagaTypes;
    }

    /**
     * Creates the list of saga types being started by the message.
     */
    private Collection<SagaType> getSagasBeingStarted(final Object message) {
        Collection<SagaType> sagaTypes = new ArrayList<>();
        Collection<Class<? extends Saga>> startingSagaTypes = messagesStartingSagas.get(message.getClass());
        for (Class<? extends Saga> sagaType : startingSagaTypes) {
            sagaTypes.add(SagaType.startsNewSaga(sagaType));
        }

        return sagaTypes;
    }

    /**
     * Populate internal map to translate between incoming message event type and saga type.
     */
    private void initializeMessageMappings(final Map<Class<? extends Saga>, SagaHandlersMap> handlersMap) {
        for (Map.Entry<Class<? extends Saga>, SagaHandlersMap> entry : handlersMap.entrySet()) {
            Class<? extends Saga> sagaClass = entry.getKey();

            Collection<MessageHandler> sagaHandlers = entry.getValue().messageHandlers();
            for (MessageHandler handler : sagaHandlers) {

                // remember all message types where a completely new saga needs to be started.
                if (handler.getStartsSaga()) {
                    messagesStartingSagas.put(handler.getMessageType(), sagaClass);
                } else {
                    messagesToContinueSaga.put(handler.getMessageType(), sagaClass);
                }
            }
        }
    }
}