package com.codebullets.sagalib;

/**
 * Base implementation of the {@link Saga} interface.
 */
public abstract class AbstractSaga<SAGA_STATE extends SagaState> implements Saga<SAGA_STATE> {
    private SAGA_STATE state;
    private boolean completed;

    /**
     * Generates a new instance of AbstractSaga.
     */
    protected AbstractSaga() {
        completed = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SAGA_STATE state() {
        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(SAGA_STATE state) {
        this.state = state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Marks this saga as completed. Resulting in the deletion of all saga state
     * associated.
     */
    protected void setAsCompleted() {
        completed = true;
    }
}
