package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;

/**
 * Invoke the event handler method on the target saga based on the
 * annotations on the public methods.
 */
public interface HandlerInvoker {
    /**
     * Invokes the handler method on the target saga. If no handler method is
     * found do nothing.
     */
    void invoke(Saga saga, Object message);
}
