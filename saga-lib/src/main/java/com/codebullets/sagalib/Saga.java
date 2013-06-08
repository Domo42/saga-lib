package com.codebullets.sagalib;

/**
 * Represents a single saga. A saga contains state and handles different messages and events.
 */
public interface Saga<SAGA_STATE extends SagaState> {
    /**
     * Gets the current state of the saga.
     */
    SAGA_STATE state();

    /**
     * Indicates whether the saga has completed.
     * @return True if saga is complete; otherwise false.
     */
    boolean isCompleted();
}
