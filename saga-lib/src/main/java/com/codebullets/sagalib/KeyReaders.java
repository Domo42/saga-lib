/*
 * Copyright 2014 Stefan Domnanovits
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

/**
 * Utility class to create new key readers.
 */
public final class KeyReaders {
    private KeyReaders() { }

    /**
     * Create a new key reader for a specific message class.
     * @param messageClass The class of the message for which a key reader is to be created.
     * @param extractFunction The function to use to extract a saga instance key from a message.
     * @param <MESSAGE> The type of the message this reader is for.
     * @param <KEY> The type of the key to be extracted.
     * @return Returns a new key reader instance.
     */
    public static <MESSAGE, KEY> KeyReader<MESSAGE, KEY> forMessage(
            final Class<MESSAGE> messageClass,
            final KeyExtractFunction<MESSAGE, KEY> extractFunction) {
        return FunctionKeyReader.create(messageClass, extractFunction);
    }

    /**
     * Create a new key reader for a specific message class.
     *
     * @param messageClass    The class of the message for which a key reader is to be created.
     * @param extractFunction The function to use to extract a saga instance key from a message.
     * @param <MESSAGE>       The type of the message this reader is for.
     * @param <KEY>           The type of the key to be extracted.
     * @return Returns a new key reader instance.
     */
    public static <MESSAGE, KEY> KeyReader<MESSAGE, KEY> forMessage(
            final Class<MESSAGE> messageClass,
            final ContextKeyExtractFunction<MESSAGE, KEY> extractFunction) {
        return FunctionKeyReader.create(messageClass, extractFunction);
    }
}