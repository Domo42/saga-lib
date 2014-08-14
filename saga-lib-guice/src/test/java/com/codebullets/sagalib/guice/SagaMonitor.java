/*
 * Copyright 2013 Stefan Domnanovits
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Custom class to monitor saga calls.
 */
public class SagaMonitor {
    private CountDownLatch sagaStarted = new CountDownLatch(1);
    private boolean sagaHasStarted;
    private boolean hasModuleStarted;

    public boolean getSagaHasStarted() {
        return sagaHasStarted;
    }

    public void setSagaHasStarted() {
        sagaHasStarted = true;
        sagaStarted.countDown();
    }

    public void moduleHasStarted() {
        hasModuleStarted = true;
    }

    public boolean hasModuleStarted() {
        return hasModuleStarted;
    }

    public boolean waitForSagaStarted(long timeout, TimeUnit unit) throws InterruptedException {
        return sagaStarted.await(timeout, unit);
    }
}