package com.codebullets.sagalib;

/**
 * Defines a way to read the saga instance key from a message.
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