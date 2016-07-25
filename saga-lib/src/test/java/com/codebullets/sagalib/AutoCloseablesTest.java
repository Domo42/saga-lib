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
package com.codebullets.sagalib;

import org.junit.Test;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AutoCloseablesTest {

    @Test
    public void closeQuietly_anAutoCloseable_close() throws Exception {
        // given
        AutoCloseable closeable = mock(AutoCloseable.class);

        // when
        AutoCloseables.closeQuietly(closeable);

        // then
        verify(closeable).close();
    }

    @Test
    public void closeQuietly_autoCloseableThrows_doNotPropagate() throws Exception {
        // given
        AutoCloseable closeable = mock(AutoCloseable.class);
        doThrow(IllegalStateException.class).when(closeable).close();

        // when
        catchException(() -> AutoCloseables.closeQuietly(closeable));

        // then
        assertThat("Expected no exception to be thrown", caughtException(), nullValue());
    }

    @Test
    public void closeQuietly_nullInstance_noException() throws Exception {
        // given
        final AutoCloseable closeable = null;

        // when
        catchException(() -> AutoCloseables.closeQuietly(closeable));

        // then
        assertThat("Expected no exception to be thrown", caughtException(), nullValue());
    }

    @Test
    public void closeQuietly_anObjectImplementingAutoCloseable_close() throws Exception {
        // given
        AutoCloseable closeable = mock(AutoCloseable.class);
        Object anyObject = closeable;

        // when
        AutoCloseables.closeQuietly(anyObject);

        // then
        verify(closeable).close();
    }

    @Test
    public void closeQuietly_anObjectNotImplementingAutoCloseable_close() throws Exception {
        // given
        Object anyObject = new Object();

        // when
        catchException(() -> AutoCloseables.closeQuietly(anyObject));

        // then
        assertThat("Expected no exception to be thrown", caughtException(), nullValue());
    }
}