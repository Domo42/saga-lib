package com.codebullets.sagalib;

/**
 * Defines a way to read the saga instance key from a message.
 *
 * @param <MESSAGE> The type of message to read the key from.
 */
public interface KeyReader<MESSAGE> {

    /**
     * Read the saga instance key from the provided message.
     */
    String readKey(MESSAGE message);

    /**
     * Gets the class associated with the reader.
     */
    Class<MESSAGE> getMessageClass();
}