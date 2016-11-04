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

import com.codebullets.sagalib.AbstractAutoTypedHandler;

import java.util.Map;

/**
 * Handles a message using the auto resolve type capabilities of the saga-lib.
 */
public class AutoTypedHandler extends AbstractAutoTypedHandler<Double> {
    static final String CALLED_KEY = "AutoTypedHandler.called.key";
    private final Map<String, String> context;

    public AutoTypedHandler(final Map<String, String> context) {
        this.context = context;
    }

    @Override
    public void handle(final Double event) {
        context.put(CALLED_KEY, "true");
    }
}
