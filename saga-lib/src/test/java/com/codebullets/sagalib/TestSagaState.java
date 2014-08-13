package com.codebullets.sagalib;

/**
 * State used during tests.
 */
public class TestSagaState extends AbstractSagaState {
    private boolean timeoutHandled;

    /**
     * Generates a new instance of TestSagaState.
     */
    public TestSagaState() {
    }

    public TestSagaState(final String instanceKey) {
        addInstanceKey(instanceKey);
    }

    public boolean isTimeoutHandled() {
        return timeoutHandled;
    }

    public void setTimeoutHandled(final boolean timeoutHandled) {
        this.timeoutHandled = timeoutHandled;
    }
}
