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

package com.codebullets.sagalib.description;

import com.codebullets.sagalib.AbstractAutoTypedHandler;
import org.openjdk.jmh.infra.Blackhole;

public class AutoTypedHandler extends AbstractAutoTypedHandler<AutoTypedMessage> {
    private static int COUNT = 0;
    private final Blackhole bh;

    public AutoTypedHandler(final Blackhole bh) {
        this.bh = bh;
    }

    @Override
    public void handle(final AutoTypedMessage event) {
        COUNT++;
        bh.consume(COUNT);
    }
}
