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
 */
public final class FunctionKeyReader<MESSAGE> implements KeyReader<MESSAGE> {
    private final KeyReadFunction<MESSAGE> readFunction;
    private final Class<MESSAGE> messageClass;

    /**
     * Generates a new instance of FunctionKeyReader.
     */
    private FunctionKeyReader(final Class<MESSAGE> messageClass, final KeyReadFunction<MESSAGE> readFunction) {
        this.messageClass = messageClass;
        this.readFunction = readFunction;
    }

    /**
     * Read the saga instance key from the provided message.
     */
    @Override
    @Nullable
    public String readKey(final MESSAGE message) {
        return readFunction.key(message);
    }

    /**
     * Gets the class associated with the reader.
     */
    @Override
    public Class<MESSAGE> getMessageClass() {
        return messageClass;
    }

    /**
     * Creates new key capable of extracting a saga instance key from a message.
     */
    public static <MESSAGE> FunctionKeyReader<MESSAGE> create(final Class<MESSAGE> messageClazz, final KeyReadFunction<MESSAGE> readFunction) {
        return new FunctionKeyReader<>(messageClazz, readFunction);
    }
}