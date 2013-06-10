package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.Saga;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Holds the list of handles messages based on saga type.
 */
public class SagaMessageMap {
    private final Class<? extends Saga> sagaType;
    private final Collection<Class> supportedMessages = new HashSet<>();

    /**
     * Generates a new instance of SagaMessageMap.
     */
    public SagaMessageMap(Class<? extends Saga> sagaType) {
        this.sagaType = sagaType;
    }

    /**
     * Gets the type of saga handling the message events.
     */
    public Class<? extends Saga> getSagaType() {
        return sagaType;
    }

    /**
     * Add a single message type to be supported by a saga type. Appends to the list
     * of supported types of a saga.
     */
    public void add(Class supportedMessage) {
        supportedMessages.add(supportedMessage);
    }

    /**
     * Returns a collection of message types handled by a saga.
     */
    public Collection<Class> messagesHandlesBySaga() {
        return Collections.unmodifiableCollection(supportedMessages);
    }
}
