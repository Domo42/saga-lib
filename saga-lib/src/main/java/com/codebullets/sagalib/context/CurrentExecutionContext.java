/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.context;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;

/**
 * Allows editing of current execution context data.
 */
public interface CurrentExecutionContext extends ExecutionContext {
    /**
     * Sets the current message being handled.
     */
    void setMessage(Object message);

    /**
     * Sets the current saga handler instance being executed.
     */
    void setSaga(Saga saga);

    /**
     * Sets the parent context of the execution.
     */
    void setParentContext(ExecutionContext parentContext);

    /**
     * Sets optional error information, that occurred during handling.
     */
    void setError(final Exception error);
}