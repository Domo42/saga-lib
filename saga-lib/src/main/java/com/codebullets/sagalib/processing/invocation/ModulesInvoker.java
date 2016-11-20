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
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
final class ModulesInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(ModulesInvoker.class);

    private final List<Supplier<Optional<Exception>>> finishers;
    private final List<BiFunction<Object, Throwable, Optional<Exception>>> errorHandlers;

    private ModulesInvoker(
            final List<Supplier<Optional<Exception>>> finishers,
            final List<BiFunction<Object, Throwable, Optional<Exception>>> errorHandlers) {
        this.finishers = Collections.unmodifiableList(finishers);
        this.errorHandlers = Collections.unmodifiableList(errorHandlers);
    }

    /**
     * Executes start on a list of modules.
     * <p>In case of an error during start, calls error and finished on already started/starting modules.</p>
     * @return Invoker in case all modules started without error
     */
    static StartResult start(final ExecutionContext context, final Iterable<SagaModule> modules) {
        Exception possibleStartException = null;
        List<Supplier<Optional<Exception>>> finishers = new ArrayList<>();
        List<BiFunction<Object, Throwable, Optional<Exception>>> errorHandlers = new ArrayList<>();

        try {
            for (final SagaModule module : modules) {
                finishers.add(createFinisher(module, context));
                errorHandlers.add(createErrorHandler(module, context));

                module.onStart(context);
            }
        } catch (Exception ex) {
            possibleStartException = ex;
        }

        return new StartResult(possibleStartException, new ModulesInvoker(finishers, errorHandlers));
    }

    /**
     * Call finishers on all started modules.
     */
    Collection<Exception> finish() {
        return Lists.reverse(finishers).stream()
                .flatMap(f -> f.get().map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    /**
     * Execute error method on started modules.
     * @return Returns possible errors triggered during error handing itself
     */
    public Collection<Exception> error(final Object message, final Throwable error) {
        return Lists.reverse(errorHandlers).stream()
                .flatMap(f -> f.apply(message, error).map(Stream::of).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    private static Supplier<Optional<Exception>> createFinisher(final SagaModule module, final ExecutionContext context) {
        return () -> tryExecute(() -> module.onFinished(context), module);
    }

    private static BiFunction<Object, Throwable, Optional<Exception>> createErrorHandler(final SagaModule module, final ExecutionContext context) {
        return (message, throwable) -> tryExecute(() -> module.onError(context, message, throwable), module);
    }

    /**
     * Executes the provided runnable without throwing possible exceptions.
     * @return Returns the exception encountered during execution.
     */
    private static Optional<Exception> tryExecute(final Runnable runnable, final SagaModule module) {
        Exception executionException;

        try {
            runnable.run();
            executionException = null;
        } catch (Exception ex) {
            executionException = ex;
            LOG.error("Error executing function on module {}", module, ex);
        }

        return Optional.ofNullable(executionException);
    }

    /**
     * Holds the modules invoker as well as the possible exception
     * starting other modules.
     */
    static class StartResult {
        @Nullable
        private final Exception startError;
        private final ModulesInvoker invoker;

        StartResult(@Nullable final Exception startError, final ModulesInvoker modulesInvoker) {
            this.startError = startError;
            this.invoker = modulesInvoker;
        }

        @Nullable
        Optional<Exception> error() {
            return Optional.ofNullable(startError);
        }

        ModulesInvoker getInvoker() {
            return invoker;
        }
    }
}
