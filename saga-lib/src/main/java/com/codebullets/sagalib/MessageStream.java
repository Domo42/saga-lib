package com.codebullets.sagalib;

/**
 * Add messages and events to the steam so that they are processed and
 * trigger saga events.
 */
public interface MessageStream {
    /**
     * Add a new message to be processed by the saga lib. The message can be of any type.
     */
    void add(Object message);
}
