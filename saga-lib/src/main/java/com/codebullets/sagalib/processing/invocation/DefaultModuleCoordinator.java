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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * This is the default implementation to trigger module callbacks.
 * It stores the number of started modules and makes sure to call
 * the error and finish handlers in reverse order they have been started.
 */
public class DefaultModuleCoordinator implements ModuleCoordinator {
    private final Iterable<SagaModule> sagaModules;
    private ModulesInvoker modulesInvoker;

    /**
     * Do not auto instantiate the list. It is only needed on exceptional cases.
     * No need to always bother the GC with it.
     */
    private Collection<Exception> encounteredExceptions;

    /**
     * Create a new instance of a module coordinator.
     */
    public DefaultModuleCoordinator(final Iterable<SagaModule> sagaModules) {
        this.sagaModules = sagaModules;
    }

    @Override
    public void start(final ExecutionContext context) throws Exception {
        ModulesInvoker.StartResult start = ModulesInvoker.start(context, sagaModules);
        modulesInvoker = start.getInvoker();
        Optional<Exception> error = start.error();
        if (error.isPresent()) {
            throw error.get();
        }
    }

    @Override
    public void onError(final ExecutionContext context, final Object message, final Exception error) {
        addException(error);

        if (modulesInvoker != null) {
            Collection<Exception> moduleErrors = modulesInvoker.error(message, error);
            addExceptions(moduleErrors);
        }
    }

    @Override
    public void finish(final ExecutionContext context) throws Exception {
        if (modulesInvoker != null) {
            Collection<Exception> finishErrors = modulesInvoker.finish();
            if (!finishErrors.isEmpty()) {
                encounteredExceptions().addAll(finishErrors);
            }

            throwInCaseOfErrors(context);
        }
    }

    private void throwInCaseOfErrors(final ExecutionContext context) throws Exception {
        if (hasEncounteredExceptions()) {
            SagaExecutionErrorsException.rethrowOrThrowIfMultiple(
                    "Multiple error encountered handling message " + context.saga(),
                    encounteredExceptions());
        }
    }

    private void addException(final Exception e) {
        encounteredExceptions().add(e);
    }

    private void addExceptions(final Collection<Exception> exceptions) {
        encounteredExceptions().addAll(exceptions);
    }

    private Collection<Exception> encounteredExceptions() {
        if (encounteredExceptions == null) {
            encounteredExceptions = new ArrayList<>();
        }

        return encounteredExceptions;
    }

    private boolean hasEncounteredExceptions() {
        return encounteredExceptions != null && !encounteredExceptions.isEmpty();
    }
}
