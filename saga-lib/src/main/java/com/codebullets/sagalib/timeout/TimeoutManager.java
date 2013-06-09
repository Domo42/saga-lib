package com.codebullets.sagalib.timeout;

import com.codebullets.sagalib.messages.Timeout;

/**
 * Triggers timeout events after the time they have been requested.
 */
public interface TimeoutManager {
    /**
     * Request a timeout event to be triggered in the future.
     * @param timeout Contains the timeout data.
     * @param callback The method to call as the timeout has expired.
     */
    void requestTimeout(Timeout timeout, TimeoutExpired callback);
}
