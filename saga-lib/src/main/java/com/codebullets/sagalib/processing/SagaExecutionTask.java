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
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.codebullets.sagalib.context.NeedContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Object message;
    private final Map<String, Object> headers;
    private final SagaEnvironment env;

    /**
     * Generates a new instance of SagaExecutionTask.
     */
    public SagaExecutionTask(
            final SagaEnvironment environment,
            final HandlerInvoker invoker,
            final Object message,
            final Map<String, Object> headers) {
        this.env = environment;
        this.invoker = invoker;
        this.message = message;
        this.headers = headers;
    }

    /**
     * Performs synchronous saga handling of the message provided in ctor.
     *
     * @throws InvocationTargetException Thrown when invocation of the handler method fails.
     * @throws IllegalAccessException Thrown when access to the handler method fails.
     */
    public void handle() throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handleSagaMessage(message);
    }

    /**
     * Perform handling of a single message.
     */
    private void handleSagaMessage(final Object invokeParam) throws InvocationTargetException, IllegalAccessException {
        Collection<SagaInstanceDescription> sagaDescriptions = env.sagaFactory().create(invokeParam);
        if (!sagaDescriptions.isEmpty()) {
            executeMessage(invokeParam, sagaDescriptions);
        } else {
            DeadMessage deadMessage = new DeadMessage(invokeParam);
            sagaDescriptions = env.sagaFactory().create(deadMessage);
            if (!sagaDescriptions.isEmpty()) {
                executeMessage(deadMessage, sagaDescriptions);
            } else {
                LOG.warn("No saga found to handle message. (message = {})", invokeParam);
            }
        }
    }

    private void executeMessage(final Object invokeParam, final Iterable<SagaInstanceDescription> sagaDescriptions)
            throws InvocationTargetException, IllegalAccessException {
        CurrentExecutionContext context = env.contextProvider().get();
        context.setMessage(invokeParam);
        setHeaders(context);

        try {
            moduleStarts(context);
            invokeSagas(context, sagaDescriptions, invokeParam);
        } catch (Exception ex) {
            moduleError(context, invokeParam, ex);
            throw ex;
        } finally {
            moduleFinished(context);
        }
    }

    private void invokeSagas(final CurrentExecutionContext context, final Iterable<SagaInstanceDescription> sagaDescriptions, final Object invokeParam)
            throws InvocationTargetException, IllegalAccessException {
        for (SagaInstanceDescription sagaDescription : sagaDescriptions) {
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

    private void interceptorStart(final SagaInstanceDescription sagaDescription, final ExecutionContext context, final Object invokeParam) {
        if (sagaDescription.isStarting()) {
            for (SagaLifetimeInterceptor interceptor : env.interceptors()) {
                interceptor.onStarting(sagaDescription.getSaga(), context, invokeParam);
            }
        }
    }

    private void moduleError(final ExecutionContext context, final Object invokeParam, final Exception ex) {
        for (SagaModule module : env.modules()) {
            module.onError(context, invokeParam, ex);
        }
    }

    private void moduleStarts(final ExecutionContext context) {
        for (SagaModule module : env.modules()) {
            module.onStart(context);
        }
    }

    private void moduleFinished(final ExecutionContext context) {
        for (SagaModule module : env.modules()) {
            module.onFinished(context);
        }
    }

    private void setHeaders(final CurrentExecutionContext context) {
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            context.setHeaderValue(entry.getKey(), entry.getValue());
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
    private void updateStateStorage(final SagaInstanceDescription description) {
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
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object message() {
        return message;
    }
}