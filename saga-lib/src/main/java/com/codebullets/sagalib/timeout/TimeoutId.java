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

/**
 * Uniquely identifies a timeout. An implementation of timeout id
 * has to act as a value type. This means two instances have return the
 * same hash code and to compare as equal if the identify the same
 * timeout.
 */
public interface TimeoutId {
    /**
     * Compares whether two timeout ids are equal. Two timeout id instances
     * representing the same timeout have to compare with equal true.
     * @param obj Any other object instance.
     * @return True if timeouts ids are equals; otherwise false
     */
    boolean equals(Object obj);

    /**
     * Returns a hashcode of of the timeout id. Two timeout ID instances
     * representing the same timeout have to return the same hash code.
     * @return a hash code value for this object.
     */
    int hashCode();
}