package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;

public class CustomInterceptor implements SagaLifetimeInterceptor {
    private boolean startingHasBeenCalled = false;
    private boolean hasExecutingBeenCalled = false;

    public boolean hasStartingBeenCalled() {
        return startingHasBeenCalled;
    }

    public boolean hasExecutingBeenCalled() {
        return hasExecutingBeenCalled;
    }

    @Override
    public void onStarting(final Saga<?> saga, final ExecutionContext context, final Object message) {
        startingHasBeenCalled = true;
    }

    @Override
    public void onHandlerExecuting(final Saga<?> saga, final ExecutionContext context, final Object message) {
        hasExecutingBeenCalled = true;
    }

    @Override
    public void onHandlerExecuted(final Saga<?> saga, final ExecutionContext context, final Object message) {
    }

    @Override
    public void onFinished(final Saga<?> saga, final ExecutionContext context) {

    }
}