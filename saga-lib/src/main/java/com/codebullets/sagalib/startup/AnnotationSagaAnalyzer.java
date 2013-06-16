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
package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.EventHandler;
import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.StartsSaga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Scans the provided saga types for {@link com.codebullets.sagalib.StartsSaga} and
 * {@link com.codebullets.sagalib.EventHandler} annotations.
 */
public class AnnotationSagaAnalyzer implements SagaAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSagaAnalyzer.class);
    private final Object sync = new Object();
    private final TypeScanner scanner;
    private Map<Class<? extends Saga>, SagaHandlersMap> scanResult;

    /**
     * Constructs a new AnnotationSagaAnalyzer instance.
     */
    @Inject
    public AnnotationSagaAnalyzer(final TypeScanner typeScanner) {
        scanner = typeScanner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Class<? extends Saga>, SagaHandlersMap> scanHandledMessageTypes() {
        if (scanResult == null) {
            populateSagaHandlers();
        }

        return scanResult;
    }

    /**
     * Creates entries in the scan result map containing the messages handlers
     * of the sagas provided by the injected scanner.
     */
    private void populateSagaHandlers() {
        synchronized (sync) {
            if (scanResult == null) {
                scanResult = new HashMap<>();
                Collection<Class<? extends Saga>> sagaTypes = scanner.scanForSagas();

                for (Class<? extends Saga> sagaType : sagaTypes) {
                    SagaHandlersMap messageHandlers = determineMessageHandlers(sagaType);
                    scanResult.put(sagaType, messageHandlers);
                }
            }
        }
    }

    /**
     * Checks all methods for saga annotations.
     */
    private SagaHandlersMap determineMessageHandlers(final Class<? extends Saga> sagaType) {
        SagaHandlersMap handlerMap = new SagaHandlersMap(sagaType);

        Method[] methods = sagaType.getMethods();
        for (Method method : methods) {
            if (isHandlerMethod(method)) {

                // method matches expected handler signature -> add to handler map
                Class<?> handlerType = method.getParameterTypes()[0];
                boolean isSagaStart = method.isAnnotationPresent(StartsSaga.class);
                handlerMap.add(new MessageHandler(handlerType, method, isSagaStart));
            }
        }

        return handlerMap;
    }

    /**
     * Checks whether method has expected annotation arguments as well as signature.
     */
    private boolean isHandlerMethod(final Method method) {
        boolean isHandler = false;

        if (method.isAnnotationPresent(StartsSaga.class) || method.isAnnotationPresent(EventHandler.class)) {
            if (method.getReturnType().equals(Void.TYPE)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length == 1) {
                    isHandler = true;
                }
                else {
                    LOG.warn("Method {}.{} marked for saga does not have the expected single parameter.", method.getDeclaringClass(), method.getName());
                }
            }
            else {
                LOG.warn("Method {}.{} marked for saga event handling but does return a value.", method.getDeclaringClass(), method.getName());
            }
        }

        return isHandler;
    }
}
