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
package com.codebullets.sagalib;

import javax.annotation.Nullable;

/**
 * Defines a way to read the saga instance key from a message. This key reader takes
 * the message class and function to extract the key as ctor arguments.
 * Use the static {@link #create(Class, KeyReadFunction)} method
 * to create a new instance.
 *
 * @param <MESSAGE> The type of the message to read the key from.
 * @param <KEY> The type of the key to be read.
 */
public final class FunctionKeyReader<MESSAGE, KEY> implements KeyReader<MESSAGE, KEY> {
    private final KeyExtractFunction<MESSAGE, KEY> extractFunction;
    private final Class<MESSAGE> messageClass;

    /**
     * Generates a new instance of FunctionKeyReader.
     */
    private FunctionKeyReader(final Class<MESSAGE> messageClass, final KeyExtractFunction<MESSAGE, KEY> extractFunction) {
        this.messageClass = messageClass;
        this.extractFunction = extractFunction;
    }

    /**
     * Read the saga instance key from the provided message.
     */
    @Override
    @Nullable
    public KEY readKey(final MESSAGE message) {
        return extractFunction.key(message);
    }

    /**
     * Gets the class associated with the reader.
     */
    @Override
    public Class<MESSAGE> getMessageClass() {
        return messageClass;
    }

    /**
     * Creates new extractor capable of extracting a saga instance key from a message.
     */
    public static <MESSAGE, KEY> FunctionKeyReader<MESSAGE, KEY> create(final Class<MESSAGE> messageClazz, final KeyReadFunction<MESSAGE> readFunction) {
        return new FunctionKeyReader<>(messageClazz, (KeyExtractFunction<MESSAGE, KEY>) readFunction);
    }

    /**
     * Creates new extractor capable of extracting a saga instance key from a message.
     */
    public static <MESSAGE, KEY> FunctionKeyReader<MESSAGE, KEY> create(
            final Class<MESSAGE> messageClazz,
            final KeyExtractFunction<MESSAGE,
            KEY> readFunction) {
        return new FunctionKeyReader<>(messageClazz, readFunction);
    }
}