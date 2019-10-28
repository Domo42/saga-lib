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

import com.codebullets.sagalib.Saga;

import java.lang.reflect.InvocationTargetException;

/**
 * Invoke the event handler method on the target saga based on the
 * annotations on the public methods.
 */
public interface HandlerInvoker {
    /**
     * Invokes the handler method on the target saga. If no handler method is
     * found do nothing.
     *
     * @throws InvocationTargetException Thrown when invocation of the handler method fails.
     * @throws IllegalAccessException Thrown when access to the handler method fails.
     */
    void invoke(Saga saga, Object message) throws InvocationTargetException, IllegalAccessException;

    /**
     * Invokes the handler method on the target saga. If no handler is found do nothing.
     *
     * <p>This method provides more information compared to {@link #invoke(Saga, Object)},
     * which is called as part of the default implementation. Implementors can use this method
     * to obtain more information about the expected invocation.</p>
     *
     * <p>This is the method called to trigger the actual invocation. If custom implementations choose to
     * override this method it is expected that {@link #invoke(Saga, Object)} does nothing.</p>
     *
     * @throws InvocationTargetException Thrown when invocation of the handler method fails.
     * @throws IllegalAccessException Thrown when access to the handler method fails.
     */
    default void invoke(InvocationContext invocationContext) throws InvocationTargetException, IllegalAccessException {
        invoke(invocationContext.saga(), invocationContext.message());
    }
}
