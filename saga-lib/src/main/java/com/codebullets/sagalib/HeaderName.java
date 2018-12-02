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

package com.codebullets.sagalib;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

/**
 * A typed Header represented by a name.
 *
 * <p>The name is what makes headers unique. The type is used as a
 * compile type check only. It is not available at runtime.</p>
 *
 * @param <T> The type of the value associated with the header.
 */
@Immutable
@SuppressWarnings("UnusedDeclaration")
public final class HeaderName<T> {
    private static final long serialVersionUID = 1L;
    private final String name;

    private HeaderName(final String name) {
        this.name = name;
    }

    /**
     * Creates a new {@code HeaderName} instance with the provided name
     * used as key.
     */
    public static <T> HeaderName<T> forName(final String name) {
        return new HeaderName<>(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        boolean isEqual = false;

        if (obj == this) {
            isEqual = true;
        } else if (obj instanceof HeaderName) {
            isEqual = Objects.equals(this.name, ((HeaderName) obj).name);
        }

        return isEqual;
    }

    @Override
    public String toString() {
        return name;
    }
}
