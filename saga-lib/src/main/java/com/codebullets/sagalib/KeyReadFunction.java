package com.codebullets.sagalib;

/**
 * Called when the key needs to be extracted from a message.
 */
public interface KeyReadFunction<MESSAGE> {
    /**
     * Returns the key value to identify a running saga.
     */
    String key(MESSAGE message);
}