package com.codebullets.sagalib;

/**
 * Called when the key needs to be extracted from a message.
 *
 * @param <MESSAGE> The type of message to read the key from.
 */
public interface KeyReadFunction<MESSAGE> {
    /**
     * Returns the key value to identify a running saga.
     */
    String key(MESSAGE message);
}