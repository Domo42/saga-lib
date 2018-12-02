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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class HeaderNameTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void equals_twoHeadersWithSameName_areEqual() {
        // given
        final String headerName = "theHeaderName";
        HeaderName<Integer> header1 = HeaderName.forName(headerName);
        HeaderName<Integer> header2 = HeaderName.forType(Integer.class, headerName);

        // when
        boolean isEqual = header1.equals(header2);

        // then
        assertThat("Expected headers to be considered equal.", isEqual, is(true));
    }

    @Test
    void equals_twoHeadersDifferentName_areNotEqual() {
        // given
        HeaderName<Integer> header1 = HeaderName.forName("header1");
        HeaderName<Integer> header2 = HeaderName.forType(Integer.class,"header2");

        // when
        boolean isEqual = header1.equals(header2);

        // then
        assertThat("Expected headers to be considered NOT equal.", isEqual, is(false));
    }
}