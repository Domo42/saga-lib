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
package com.codebullets.sagalib.perftest.saga;

import com.codebullets.sagalib.KeyReader;
import com.codebullets.sagalib.perftest.messages.AbstractTestMessage;

import javax.annotation.Nullable;

public class TestKeyReader<T extends AbstractTestMessage> implements KeyReader<T, String> {
    private final Class<T> clazz;

    /**
     * Generates a new instance of TestKeyReader.
     */
    public TestKeyReader(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Nullable
    @Override
    public String readKey(final T t) {
        return t.getCorrelationId();
    }

    @Override
    public Class<T> getMessageClass() {
        return clazz;
    }

    /**
     * Create test key reader instance.
     */
    public static <T extends AbstractTestMessage> TestKeyReader<T> create(final Class<T> clazz) {
        return new TestKeyReader<>(clazz);
    }
}