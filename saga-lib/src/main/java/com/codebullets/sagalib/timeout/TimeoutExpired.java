package com.codebullets.sagalib.timeout;

import com.codebullets.sagalib.messages.Timeout;

/**
 * Callback interface triggered by the {@link TimeoutManager} as a timeout has expired.
 */
public interface TimeoutExpired {
    /**
     * Called as a timeout has expired.
     * @param timeout The timeout event containing the original timeout request data.
     */
    void expired (Timeout timeout);
}
