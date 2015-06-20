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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests for {@link UUIDTimeoutId} class.
 */
public class UUIDTimeoutIdTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * <pre>
     * Given => Two different id instances, using the same UUID
     * When  => equals is called
     * Then  => equals returns true
     * </pre>
     */
    @Test
    public void equals_twoDifferentInstancesSameId_returnsTrue() {
        // given
        UUID uuid = UUID.randomUUID();
        UUIDTimeoutId instance1 = new UUIDTimeoutId(uuid);
        UUIDTimeoutId instance2 = new UUIDTimeoutId(uuid);

        // when
        boolean isEqual = instance1.equals(instance2);

        // then
        assertThat("Expected ids to be equal.", isEqual, equalTo(true));
    }

    /**
     * <pre>
     * Given => Two different id instances, using the different UUID
     * When  => equals is called
     * Then  => equals returns false
     * </pre>
     */
    @Test
    public void equals_twoDifferentInstancesDifferentIds_returnsFalse() {
        // given
        UUIDTimeoutId instance1 = UUIDTimeoutId.generateNewId();
        UUIDTimeoutId instance2 = UUIDTimeoutId.generateNewId();

        // when
        boolean isEqual = instance1.equals(instance2);

        // then
        assertThat("Expected ids not to be equal.", isEqual, equalTo(false));
    }

    /**
     * <pre>
     * Given => one is instance
     * When  => instance is compared with itself
     * Then  => equals returns true
     * </pre>
     */
    @Test
    public void equals_sameInstance_returnsTrue() {
        // given
        UUIDTimeoutId instance = UUIDTimeoutId.generateNewId();

        // when
        boolean isEqual = instance.equals(instance);

        // then
        assertThat("Expected instance to be equal with itself.", isEqual, equalTo(true));
    }

    /**
     * <pre>
     * Given => Two different id instances using the same UUID
     * When  => hashCode of both is queried
     * Then  => expected both hashcodes to be the same.
     * </pre>
     */
    @Test
    public void hashCode_twoDifferentInstancesSameId_haveSameHashCode() {
        // given
        UUID uuid = UUID.randomUUID();
        UUIDTimeoutId instance1 = new UUIDTimeoutId(uuid);
        UUIDTimeoutId instance2 = new UUIDTimeoutId(uuid);

        // when
        int hc1 = instance1.hashCode();
        int hc2 = instance2.hashCode();

        // then
        assertThat("Expected both hash codes to be equal", hc1, equalTo(hc2));
    }

    /**
     * <pre>
     * Given => null parameter provided
     * When  => ctor is called
     * Then  => ctor throws a null pointer exception
     * </pre>
     */
    @Test
    public void ctor_nullParam_throwsException() {
        thrown.expect(Exception.class);

        // given
        UUID ctorParam = null;

        // when
        new UUIDTimeoutId(ctorParam);
    }
}