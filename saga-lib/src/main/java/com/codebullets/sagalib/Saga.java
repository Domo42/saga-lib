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
     * Sets the current state of the sage. This method is called by the saga-lib before
     * any of the handler methods are called. Once set the {@link #state()} method should return
     * the provided instance.
     */
    void setState(SAGA_STATE state);

    /**
     * Instructs the sage to create a new empty instance of the saga state. After
     * this method has been called {@link #state()} is expected to return the new instance.
     */
    void createNewState();

    /**
     * Indicates whether the saga has completed.
     * @return True if saga is complete; otherwise false.
     */
    boolean isCompleted();
}
