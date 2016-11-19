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

import com.codebullets.sagalib.AutoCloseables;
import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.processing.invocation.HandlerInvoker;
import com.codebullets.sagalib.timeout.Timeout;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Controls the saga message flow.
 */
public class SagaMessageStream implements MessageStream {
    private static final Logger LOG = LoggerFactory.getLogger(SagaMessageStream.class);
    private static final Map<String, Object> EMPTY_HEADERS = Collections.emptyMap();

    private final SagaEnvironment environment;
    private final HandlerInvoker invoker;
    private final Executor executor;

    /**
     * Creates a new SagaMessageStream instance.
     */
    @Inject
    public SagaMessageStream(
            final HandlerInvoker invoker,
            final SagaEnvironment environment,
            final Executor executor) {
        this.executor = executor;
        this.environment = environment;
        this.invoker = invoker;

        environment.timeoutManager().addExpiredCallback(SagaMessageStream.this::timeoutHasExpired);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(@Nonnull final Object message) {
        checkNotNull(message, "Message to handle must not be null.");
        add(message, EMPTY_HEADERS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(@Nonnull final Object message, final Map<String, Object> headers) {
        checkNotNull(message, "Message to handle must not be null.");

        SagaExecutionTask task = createTaskToExecute(message, headers, null);
        executor.execute(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@Nonnull final Object message) throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handle(message, null, null);
    }

    @Override
    public void handle(@Nonnull final Object message, @Nullable final ExecutionContext parentContext) throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handle(message, null, parentContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@Nonnull final Object message, @Nullable final Map<String, Object> headers) throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");
        handle(message, headers, null);
    }

    @Override
    public void handle(@Nonnull final Object message, @Nullable final Map<String, Object> headers, @Nullable final ExecutionContext parentContext)
            throws InvocationTargetException, IllegalAccessException {
        checkNotNull(message, "Message to handle must not be null.");

        Map<String, Object> executionHeaders = mergeHeaders(headers, parentContext);
        SagaExecutionTask executionTask = createTaskToExecute(message, executionHeaders, parentContext);
        try {
            executionTask.handle();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw e;
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Called whenever the timeout manager reports an expired timeout.
     */
    private void timeoutHasExpired(final Timeout timeout) {
        try {
            add(timeout);
        } catch (Exception ex) {
            LOG.error("Error handling timeout {}", timeout, ex);
        }
    }

    private SagaExecutionTask createTaskToExecute(final Object message, final Map<String, Object> headers, final ExecutionContext parentContext) {
        return new SagaExecutionTask(environment, invoker, message, headers, parentContext);
    }

    private Map<String, Object> mergeHeaders(final @Nullable Map<String, Object> headers, final @Nullable ExecutionContext parentContext) {
        Map<String, Object> executionHeaders = createHeaderMap(parentContext);
        if (headers != null) {
            for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
                executionHeaders.put(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        return executionHeaders;
    }

    private Map<String, Object> createHeaderMap(@Nullable final ExecutionContext context) {
        Map<String, Object> headers = new HashMap<>();
        if (context != null) {
            for (String headerKey : context.getHeaders()) {
                headers.put(headerKey, context.getHeaderValue(headerKey));
            }
        }

        return headers;
    }

    @Override
    public void close() {
        if (executor instanceof ExecutorService) {
            shutDownExecutor((ExecutorService) executor);
        }

        AutoCloseables.closeQuietly(environment);
        AutoCloseables.closeQuietly(invoker);
    }

    private void shutDownExecutor(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.warn("Timeout exceeded waiting for saga lib orderly executor service shutdown, forcing shutdown.", e);
            executorService.shutdownNow();
        }
    }
}
