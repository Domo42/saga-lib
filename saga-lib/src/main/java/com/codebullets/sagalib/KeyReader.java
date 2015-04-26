package com.codebullets.sagalib;

import com.codebullets.sagalib.context.LookupContext;

import javax.annotation.Nullable;

/**
 * Defines a way to read the saga instance key from a message.
 *
 * @param <MESSAGE> The type of message to read the key from.
 * @param <KEY> The type of the key returned from the message.
 */
public interface KeyReader<MESSAGE, KEY> {

    /**
     * Read the saga instance key from the provided message.
     * @return instance key of the message or null if key is empty or not found.
     */
    @Nullable
    KEY readKey(MESSAGE message, LookupContext lookupContext);

    /**
     * Gets the class associated with the reader.
     */
    Class<MESSAGE> getMessageClass();
}