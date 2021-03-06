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

public class DirectHandler<T> extends AbstractHandler<String> {
    static final String CALLED_KEY = "DirectHandler.called.key";
    private final Map<String, String> context;

    public DirectHandler(final Map<String, String> context) {
        super(String.class);
        this.context = context;
    }

    @Override
    public void handle(final String event) {
        context.put(CALLED_KEY, "true");
    }
}
