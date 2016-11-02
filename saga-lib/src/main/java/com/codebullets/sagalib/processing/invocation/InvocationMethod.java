/*
 * Copyright 2016 Stefan Domnanovits
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
package com.codebullets.sagalib.processing.invocation;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Describes the way how the target method can be accessed.
 * This is either by reflection call of a specific method, or
 * fetching the consumer method of the description.
 */
public final class InvocationMethod {
    @Nullable
    private Method method;

    private InvocationMethod(@Nullable final Method method) {
        this.method = method;
    }

    /**
     * If this method return empty, the saga needs have a custom
     * description for direct handling.
     */
    public Optional<Method> invocationMethod() {
        return Optional.ofNullable(method);
    }

    /**
     * Create a new instance indicating the saga has it's own handling description.
     */
    public static InvocationMethod selfDescribed() {
        return new InvocationMethod(null);
    }

    /**
     * Creates a new instances defining the method to be called via reflection.
     */
    public static InvocationMethod reflectionInvoked(final Method method) {
        return new InvocationMethod(method);
    }
}
