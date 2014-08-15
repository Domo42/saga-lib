package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;

public class CustomInterceptor implements SagaLifetimeInterceptor {
    private boolean startingHasBeenCalled = false;

    public boolean hasStartingBeenCalled() {
        return startingHasBeenCalled;
    }

    @Override
    public void onStarting(final Saga<?> saga, final ExecutionContext context, final Object message) {
        startingHasBeenCalled = true;
    }

    @Override
    public void onFinished(final Saga<?> saga, final ExecutionContext context) {

    }
}