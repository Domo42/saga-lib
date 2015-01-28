/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.keyintegration;

import com.codebullets.sagalib.AbstractSingleEventSaga;
import com.codebullets.sagalib.DeadMessage;
import com.codebullets.sagalib.StartsSaga;

public class DeadMessageSaga extends AbstractSingleEventSaga {
    @StartsSaga
    public void deadMessageReceived(final DeadMessage deadMessage) {
    }
}