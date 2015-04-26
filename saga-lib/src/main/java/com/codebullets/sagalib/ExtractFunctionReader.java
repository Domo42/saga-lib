/*
 * Copyright 2015 Stefan Domnanovits
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
package com.codebullets.sagalib;

import com.codebullets.sagalib.context.LookupContext;

import javax.annotation.Nullable;

/**
 * Encapsulates a {@link KeyExtractFunction} within a {@link ContextKeyExtractFunction}.
 * This just means the lookup context is skipped when search for a saga instance key.
 *
 * @param <MESSAGE> The type of the message to read the key from.
 * @param <KEY> The type of the key to be read.
 */
final class ExtractFunctionReader<MESSAGE, KEY> implements ContextKeyExtractFunction<MESSAGE, KEY> {
    private final KeyExtractFunction<MESSAGE, KEY> extractFunction;

    /**
     * Generates a new instance of ExtractFunctionReader.
     */
    private ExtractFunctionReader(final KeyExtractFunction<MESSAGE, KEY> extractFunction) {
        this.extractFunction = extractFunction;
    }

    @Nullable
    @Override
    public KEY key(final MESSAGE message, final LookupContext context) {
        return extractFunction.key(message);
    }

    public static <MESSAGE, KEY> ContextKeyExtractFunction<MESSAGE, KEY> encapsulate(final KeyExtractFunction<MESSAGE, KEY> extractFunction) {
        return new ExtractFunctionReader<>(extractFunction);
    }
}