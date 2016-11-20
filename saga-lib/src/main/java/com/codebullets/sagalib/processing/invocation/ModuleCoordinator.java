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
import com.codebullets.sagalib.context.CurrentExecutionContext;

/**
 * The implementer of this interface is responsible to call start error and finished
 * on the list of available modules.
 * <p>The coordinator is called following try-catch-finally semantics. This means
 * {@code start} is called first, in case of an error the {@code onError} method is
 * triggered. In all those cases {@code finish} is called last.</p>
 *
 * <p>The implementation is expected to be exception safe. That means
 * even if modules are throwing exceptions the implementation wil make sure all
 * other modules still get their error and finish callbacks to be able to clean
 * up their state.
 *
 * <p>On the other hand this coordinator needs to be transparent for
 * exceptions. In case of an error, it needs to be reported and not silently
 * discarded. </p>
 *
 * <p>A new instance is created for each message handled on the actual handling
 * thread. This means it is safe for a coordinator to store individual state
 * information.</p>
 */
public interface ModuleCoordinator {
    /**
     * This method called all {@code onStart} on all available modules.
     * <p>In case of an exception the expectation is that no further modules are started
     * and the originating exception is re-thrown. This thrown exception indicates a
     * module failure and will result in saga handling to be skipped.</p>
     *
     * @throws Exception Re-throws the exception encountered in any of the start error handlers.
     */
    void start(final ExecutionContext context) throws Exception;

    /**
     * This method is called in case there has been an exception during
     * the execution of saga handlers or if the modules corrdinator itself threw an
     * error during start. It is executed before {@link #finish(CurrentExecutionContext)}}.
     *
     * <p>If there is no error this method will not be called.</p>
     *
     * <p>This method is not expected to throw an exceptions. If the coordinator needs
     * to report an error during their modules error handling it can do it as
     * part of the finish handler.</p>
     */
    void onError(final ExecutionContext context, Object message, Exception error);

    /**
     * Is called after all saga handlers have been executed. In case of
     * errors during error or finisher handling, this method is expected to re-throw them.
     *
     * @throws Exception Throws an exception in case of errors during either finish or error handlers.
     */
    void finish(final ExecutionContext context) throws Exception;
}
