/*
 * Copyright 2016 Stefan Domnanovits
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.ExecutionContext;
import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.context.CurrentExecutionContext;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Executes the methods on the list of available modules. This has the following rules:
 * <ul>
 *     <li>Every module that is at least partly started will also be finished.</li>
 *     <li>If an error occurs when starting {@code onError} will be called for all
 *         started modules as well a the module raising the error.</li>
 *     <li>{@code onFinished} will be called for all started and partially started modules.</li>
 * </ul>
 */
@Immutable
public final class ModulesInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(ModulesInvoker.class);

    private final List<Runnable> finishers;
    private final List<BiConsumer<Object, Throwable>> errorHandlers;

    private ModulesInvoker(final List<Runnable> finishers, final List<BiConsumer<Object, Throwable>> errorHandlers) {
        this.finishers = Collections.unmodifiableList(finishers);
        this.errorHandlers = Collections.unmodifiableList(errorHandlers);
    }

    /**
     * Executes start on a list of modules.
     * <p>In case of an error during start, calls error and finished on already started/starting modules.</p>
     * @return Invoker in case all modules started without error
     */
    public static ModulesInvoker start(final CurrentExecutionContext context, final Iterable<SagaModule> modules) {
        List<Runnable> finishers = new ArrayList<>();
        List<BiConsumer<Object, Throwable>> errorHandlers = new ArrayList<>();

        try {
            for (final SagaModule module : modules) {
                finishers.add(createFinisher(module, context));
                errorHandlers.add(createErrorHandler(module, context));

                module.onStart(context);
            }
        } catch (Exception ex) {
            // call error and finish handlers on partially started module list.
            context.setError(ex);
            ModulesInvoker invoker = new ModulesInvoker(finishers, errorHandlers);
            invoker.error(context.message(), ex);
            invoker.finish();
            throw ex;
        }

        return new ModulesInvoker(finishers, errorHandlers);
    }

    /**
     * Call finishers on all started modules.
     */
    public void finish() {
        Lists.reverse(finishers).forEach(Runnable::run);
    }

    /**
     * Execute error method on started modules.
     */
    public void error(final Object message, final Throwable error) {
        Lists.reverse(errorHandlers).forEach(f -> f.accept(message, error));
    }

    private static Runnable createFinisher(final SagaModule module, final ExecutionContext context) {
        return () -> tryExecute(() -> module.onFinished(context), module);
    }

    private static BiConsumer<Object, Throwable> createErrorHandler(final SagaModule module, final ExecutionContext context) {
        return (message, throwable) -> tryExecute(() -> module.onError(context, message, throwable), module);
    }

    /**
     * Executes the provided runnable without throwing possible exceptions.
     */
    private static void tryExecute(final Runnable runnable, final SagaModule module) {
        try {
            runnable.run();
        } catch (Exception ex) {
            LOG.error("Error executing function on module {}", module, ex);
        }
    }
}
