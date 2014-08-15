/*
 * Copyright 2014 Stefan Domnanovits
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
package com.codebullets.sagalib.guice;

import com.codebullets.sagalib.SagaModule;
import com.codebullets.sagalib.ExecutionContext;
import com.google.inject.Inject;

/**
 * Saga module called during tests
 */
public class TestSagaModule implements SagaModule {
    private final SagaMonitor monitor;

    /**
     * Generates a new instance of TestSagaModule.
     */
    @Inject
    public TestSagaModule(final SagaMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void onStart(final ExecutionContext context) {
        monitor.moduleHasStarted();
    }

    @Override
    public void onFinished(final ExecutionContext context) {
    }

    @Override
    public void onError(final ExecutionContext context, final Object message, final Throwable error) {
    }
}