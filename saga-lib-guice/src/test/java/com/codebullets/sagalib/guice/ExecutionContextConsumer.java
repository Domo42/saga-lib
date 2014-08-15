/*
 * COPYRIGHT: FREQUENTIS AG. All rights reserved.
 *            Registered with Commercial Court Vienna,
 *            reg.no. FN 72.115b.
 */
package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.ExecutionContext;
import javax.inject.Inject;
import javax.inject.Provider;

public class ExecutionContextConsumer {
    private final Provider<ExecutionContext> contextProvider;

    /**
     * Creates a new instance of ExecutionContextConsumer.
     */
    @Inject
    public ExecutionContextConsumer(final Provider<ExecutionContext> contextProvider) {
        this.contextProvider = contextProvider;
    }

    public ExecutionContext newContext() {
        return contextProvider.get();
    }
}