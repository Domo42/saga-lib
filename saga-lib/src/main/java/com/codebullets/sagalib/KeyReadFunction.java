package com.codebullets.sagalib;

import javax.annotation.Nullable;

/**
 * Called when the key needs to be extracted from a message.
 *
 * @param <MESSAGE> The type of message to read the key from.
 */
public interface KeyReadFunction<MESSAGE> {
    /**
     * Returns the key value to identify a running saga.
     */
    @Nullable
    String key(MESSAGE message);
}