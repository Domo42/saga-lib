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

package com.codebullets.sagalib.annotations;

import com.codebullets.sagalib.AbstractSingleEventSaga;
import com.codebullets.sagalib.StartsSaga;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Simple handler using annotations for message handling
 */
public class AnnotationHandler extends AbstractSingleEventSaga {
    private static int COUNT = 0;
    private final Blackhole bh;

    public AnnotationHandler(final Blackhole bh) {
        super();
        this.bh = bh;
    }

    @StartsSaga
    public void handlerMethod(final AnnotationHandlerMessage message) {
        COUNT++;
        bh.consume(COUNT);
    }
}
