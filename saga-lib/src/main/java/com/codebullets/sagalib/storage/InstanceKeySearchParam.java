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
package com.codebullets.sagalib.storage;

import java.util.Objects;

/**
 * Contains the search parameter data to lookup all instances of
 * a single type and instance key.
 */
public final class InstanceKeySearchParam {
    private final Object instanceKey;
    private final String sagaTypeName;

    /**
     * Generates a new instance of InstanceKeySearchParam.
     */
    public InstanceKeySearchParam(final String sagaTypeName, final Object instanceKey) {
        this.instanceKey = instanceKey;
        this.sagaTypeName = sagaTypeName;
    }

    /**
     * Gets the instance to search for.
     */
    public Object getInstanceKey() {
        return instanceKey;
    }

    /**
     * Gets the qualified type name of the saga.
     */
    public String getSagaTypeName() {
        return sagaTypeName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceKey, sagaTypeName);
    }

    @Override
    public boolean equals(final Object obj) {
        boolean isEqual = false;

        if (this == obj) {
            isEqual = true;
        } else if (obj instanceof InstanceKeySearchParam) {
            InstanceKeySearchParam other = (InstanceKeySearchParam) obj;
            isEqual = Objects.equals(this.instanceKey, other.instanceKey)
                   && Objects.equals(this.sagaTypeName, other.sagaTypeName);
        }

        return isEqual;
    }

    @Override
    public String toString() {
        return "[" + sagaTypeName + "/" + instanceKey + "]";
    }
}