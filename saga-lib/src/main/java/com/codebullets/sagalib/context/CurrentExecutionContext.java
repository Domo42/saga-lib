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

    /**
     * Records the identifier of the saga stored. This includes the current
     * and parent context.
     */
    void recordSagaStateStored(final String sagaId);

    /**
     * Checks whether a saga state was stored as part of full execution chain.
     * This includes sagas handles as part of child and parent contexts.
     *
     * <p>This is used to determine whether a saga state needs
     * deletion in case the saga is finished, but has already been saved in
     * possible recursive executions.</p>
     */
    boolean hasBeenStored(final String sagaId);
}