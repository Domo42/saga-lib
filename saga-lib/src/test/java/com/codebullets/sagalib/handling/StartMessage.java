/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.handling;

public class StartMessage {
    private String instanceKey;

    public StartMessage(final String instanceKey) {
        this.instanceKey = instanceKey;
    }

    public String getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(final String instanceKey) {
        this.instanceKey = instanceKey;
    }
}
