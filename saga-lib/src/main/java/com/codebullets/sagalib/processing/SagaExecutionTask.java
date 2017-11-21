/*
 * Copyright 2013 Stefan Domnanovits
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.DeadMessage;
import com.codebullets.sagalib.ExecutedRunnable;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaLifetimeInterceptor;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.context.NeedContext;
import com.codebullets.sagalib.processing.invocation.HandlerInvoker;
import com.codebullets.sagalib.processing.invocation.ModuleCoordinator;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Perform execution of saga message handling. This class is the execution
 * root unit when handling messages as part of an execution strategy.
 */
class SagaExecutionTask implements ExecutedRunnable {
    private static final Logger LOG = LoggerFactory.getLogger(SagaExecutionTask.class);

    private final HandlerInvoker invoker;
    private final SagaEnvironment env;
    private final LookupContext taskLookupContext;

    @Nullable
    private final ExecutionContext parentContext;

    /**
     * Generates a new instance of SagaExecutionTask.
     */
    @Inject
    SagaExecutionTask(
            final SagaEnvironment environment,
            final HandlerInvoker invoker,
            final Object message,
            final Map<String, Object> headers,
            @Nullable final ExecutionContext parentContext) {
        this.parentContext = parentContext;
        this.taskLookupContext = new SagaLookupContext(message, headers, parentContext);
        this.env = environment;
        this.invoker = invoker;
    }

    /**
     * Performs synchronous saga handling of the message provided in ctor.
     *
     * @throws InvocationTargetException Thrown when invocation of the handler method fails.
     * @throws IllegalAccessException Thrown when access to the handler method fails.
     */
    public void handle() throws Exception {
        checkNotNull(taskLookupContext.message(), "Message to handle must not be null.");
        startExecutionChain(taskLookupContext);
    }

    private boolean startExecutionChain(final LookupContext messageLookupContext) throws Exception {
        boolean sagasExecuted = false;

        CurrentExecutionContext executionContext = env.contextProvider().get();
        executionContext.setMessage(messageLookupContext.message());
        executionContext.setParentContext(parentContext);
        setHeaders(executionContext);

        ModuleCoordinator moduleCoordinator = env.coordinatorFactory().create(env.modules());

        try {
            moduleCoordinator.start(executionContext);
            sagasExecuted = executeHandlersForMessage(messageLookupContext, executionContext);
            if (!sagasExecuted) {
                LOG.warn("No saga or saga state found to handle message. (message = {})", taskLookupContext.message());
            }
        } catch (Exception ex) {
            executionContext.setError(ex);
            moduleCoordinator.onError(executionContext, executionContext.message(), ex);
        } finally {
            moduleCoordinator.finish(executionContext);
        }

        return sagasExecuted;
    }

    private boolean executeHandlersForMessage(final LookupContext messageLookupContext, final CurrentExecutionContext executionContext)
            throws Exception {
        boolean sagasExecuted = false;
        Object message = executionContext.message();

        Collection<SagaInstanceInfo> sagaDescriptions = env.instanceResolver().resolve(messageLookupContext);
        if (!sagaDescriptions.isEmpty()) {
            invokeSagas(executionContext, sagaDescriptions, message);
            sagasExecuted = true;
        } else if (!(message instanceof DeadMessage)) {
            DeadMessage deadMessage = new DeadMessage(taskLookupContext.message());
            LookupContext deadMessageContext = new SagaLookupContext(deadMessage, taskLookupContext, parentContext);
            sagasExecuted = startExecutionChain(deadMessageContext);
        }

        return sagasExecuted;
    }

    private void invokeSagas(final CurrentExecutionContext context, final Iterable<SagaInstanceInfo> sagaDescriptions, final Object invokeParam)
            throws InvocationTargetException, IllegalAccessException {
        if (context.dispatchingStopped()) {
            LOG.debug("Handler dispatching stopped before invoking any saga.");
        } else {
            for (SagaInstanceInfo sagaDescription : sagaDescriptions) {
                Saga saga = sagaDescription.getSaga();
                context.setSaga(saga);
                setSagaExecutionContext(saga, context);

                // call interceptor pre handling hooks
                interceptorStart(sagaDescription, context, invokeParam);
                interceptorHandling(saga, context, invokeParam);

                // perform actual saga invoke
                invoker.invoke(saga, invokeParam);

                // call interceptor handler finished hooks
                interceptorHandlingExecuted(saga, context, invokeParam);
                interceptorFinished(saga, context);
                updateStateStorage(sagaDescription);

                if (context.dispatchingStopped()) {
                    LOG.debug("Handler dispatching stopped after invoking saga {}.", sagaDescription.getSaga().getClass().getSimpleName());
                    break;
                }
            }
        }
    }

    private void interceptorHandling(final Saga saga, final ExecutionContext context, final Object invokeParam) {
        for (SagaLifetimeInterceptor interceptor : env.interceptors()) {
            interceptor.onHandlerExecuting(saga, context, invokeParam);
        }
    }

    private void interceptorHandlingExecuted(final Saga saga, final ExecutionContext context, final Object invokeParam) {
        for (SagaLifetimeInterceptor interceptor : env.interceptors()) {
            interceptor.onHandlerExecuted(saga, context, invokeParam);
        }
    }

    private void interceptorFinished(final Saga saga, final ExecutionContext context) {
        if (saga.isFinished()) {
            for (SagaLifetimeInterceptor interceptor : env.interceptors()) {
                interceptor.onFinished(saga, context);
            }
        }
    }

    private void interceptorStart(final SagaInstanceInfo sagaDescription, final ExecutionContext context, final Object invokeParam) {
        if (sagaDescription.isStarting()) {
            for (SagaLifetimeInterceptor interceptor : env.interceptors()) {
                interceptor.onStarting(sagaDescription.getSaga(), context, invokeParam);
            }
        }
    }

    private void setHeaders(final CurrentExecutionContext context) {
        for (String key : taskLookupContext.getHeaders()) {
            context.setHeaderValue(key, taskLookupContext.getHeaderValue(key));
        }
    }

    private void setSagaExecutionContext(final Saga saga, final ExecutionContext context) {
        if (saga instanceof NeedContext) {
            ((NeedContext) saga).setExecutionContext(context);
        }
    }

    /**
     * Updates the state storage depending on whether the saga is completed or keeps on running.
     */
    private void updateStateStorage(final SagaInstanceInfo description) {
        Saga saga = description.getSaga();

        // if saga has finished delete existing state and possible timeouts
        // if saga has just been created state has never been save and there
        // is no need to delete it.
        if (saga.isFinished() && !description.isStarting()) {
            env.storage().delete(saga.state().getSagaId());
            env.timeoutManager().cancelTimeouts(saga.state().getSagaId());
        } else if (!saga.isFinished()) {
            env.storage().save(saga.state());
        }
    }

    /**
     * Similar to {@link #handle()} but intended for execution on any thread.<p/>
     * May throw a runtime exception in case something went wrong invoking the target saga message handler.
     */
    @Override
    public void run() {
        try {
            handle();
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object message() {
        return taskLookupContext.message();
    }
}