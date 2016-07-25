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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Contains static helper methods to deal with the {@code AutoCloseable} interface.
 */
public final class AutoCloseables {
    private static final Logger LOG = LoggerFactory.getLogger(AutoCloseables.class);

    private AutoCloseables() {
    }

    /**
     * Checks if any instance within {@code iterable} implements the
     * {@code AutoCloseable} interface and calls close if yes. If an
     * exception occurs it is logged instead of propagating it.
     */
    public static <T> void closeQuietly(@Nullable final Iterable<T> iterable) {
        if (iterable != null) {
            iterable.forEach(AutoCloseables::closeQuietly);
        }
    }

    /**
     * Checks if {@code object} implements the {@code AutoCloseable} interface and
     * calls close if yes. If an exception occurs it is logged
     * instead of propagating it.
     */
    public static void closeQuietly(@Nullable final Object object) {
        if (object instanceof AutoCloseable) {
            closeQuietly((AutoCloseable) object);
        }
    }

    /**
     * Calls close on the provided instance. If an exception occurs it is logged
     * instead of propagating it.
     */
    public static void closeQuietly(@Nullable final AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            LOG.warn("Error closing instance {}.", closeable, e);
        }
    }
}