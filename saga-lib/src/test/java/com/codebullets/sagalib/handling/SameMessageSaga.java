/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.handling;

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.StartsSaga;
import com.codebullets.sagalib.TestSagaState;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

public class SameMessageSaga extends AbstractSaga<TestSagaState> {
    private final AtomicInteger startCounter;
    private final AtomicInteger continueCounter;

    public SameMessageSaga(final AtomicInteger startCounter, final AtomicInteger continueCounter) {
        this.startCounter = startCounter;
        this.continueCounter = continueCounter;
    }

    @StartsSaga
    public void sagaStartup(final StartMessage startMessage) {
        state().addInstanceKey(startMessage.getInstanceKey());
        startCounter.incrementAndGet();
    }

    @EventHandler
    public void continuesSaga(final StartMessage message) {
        continueCounter.incrementAndGet();
        setFinished();
    }

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        return Collections.emptyList();
    }
}