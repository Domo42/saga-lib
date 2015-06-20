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
package com.codebullets.sagalib.timeout;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Uniquely identifies a timeout using a UUID.
 */
public final class UUIDTimeoutId implements TimeoutId, Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;

    /**
     * Generates a new instance of TimeoutId.
     */
    public UUIDTimeoutId(final UUID id) {
        checkNotNull(id, "Id parameter is not allowed to be null.");

        this.id = id;
    }

    /**
     * Generates a new unique TimeoutId instance.
     */
    public static UUIDTimeoutId generateNewId() {
        return new UUIDTimeoutId(UUID.randomUUID());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj instanceof UUIDTimeoutId) {
            isEqual = id.equals(((UUIDTimeoutId) obj).id);
        }

        return isEqual;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}