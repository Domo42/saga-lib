package com.codebullets.sagalib;

/**
 * State used during tests.
 */
public class TestSagaState extends AbstractSagaState {
    private String instanceKey;
    private boolean timeoutHandled;

    @Override
    public String instanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(final String instanceKey) {
        this.instanceKey = instanceKey;
    }

    public boolean isTimeoutHandled() {
        return timeoutHandled;
    }

    public void setTimeoutHandled(final boolean timeoutHandled) {
        this.timeoutHandled = timeoutHandled;
    }
}
