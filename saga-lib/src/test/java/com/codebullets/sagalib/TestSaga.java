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
package com.codebullets.sagalib;

import com.codebullets.sagalib.messages.Timeout;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Tests class containing expected annotations.
 */
public class TestSaga extends AbstractSaga<TestSagaState> implements Saga<TestSagaState> {
    public static final int INSTANCE_KEY = 42;
    private final TimeoutManager timeoutManager;
    private boolean startupCalled;
    private boolean handerCalled;

    /**
     * Generates a new instance of TestSaga.
     */
    public TestSaga() {
        timeoutManager = null;
    }

    public TestSaga(final TimeoutManager timeoutManager) {
        this.timeoutManager = timeoutManager;
    }

    @StartsSaga
    public void sagaStartup(String startedByString) {
        state().setInstanceKey(String.valueOf(INSTANCE_KEY));
        startupCalled = true;

        if (timeoutManager != null) {
            timeoutManager.requestTimeout(state().getSagaId(), "myTimeoutName", 5, TimeUnit.SECONDS);
        }
    }

    @EventHandler
    public void handlesIntegerType(Integer intValue) {
        handerCalled = true;
        setAsCompleted();
    }

    @EventHandler
    public void handleTimeout(Timeout timeout) {
        state().setTimoutHandled(true);
    }

    public boolean startupCalled() {
        return startupCalled;
    }

    public boolean handerCalled() {
        return handerCalled;
    }

    @Override
    public void createNewState() {
        setState(new TestSagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        KeyReader reader = FunctionKeyReader.create(
                Integer.class,
                new KeyReadFunction<Integer>() {

                    @Override
                    public String key(final Integer integer) {
                        return integer.toString();
                    }
                }
        );

        return Lists.newArrayList(reader);
    }

    public static Method startupMethod() throws NoSuchMethodException {
        return TestSaga.class.getMethod("sagaStartup", String.class);
    }

    public static Method handlerMethod() throws NoSuchMethodException {
        return TestSaga.class.getMethod("handlesIntegerType", Integer.class);
    }
}