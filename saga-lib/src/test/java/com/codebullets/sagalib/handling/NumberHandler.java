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

package com.codebullets.sagalib.handling;

import com.codebullets.sagalib.AbstractHandler;

import java.util.Map;

/**
 * Handles a base type of the actual message instead of the same type.
 */
public class NumberHandler extends AbstractHandler<Number> {
    static final String CALLED_KEY = "NumberHandler.called.key";
    private final Map<String, String> context;

    public NumberHandler(final Map<String, String> context) {
        this.context = context;
    }

    @Override
    public void handle(final Number event) {
        context.put(CALLED_KEY, "true");
    }
}
