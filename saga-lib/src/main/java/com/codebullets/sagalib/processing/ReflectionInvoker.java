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
package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * Invokes the target handler on the provided saga for the given handler.
 */
public class ReflectionInvoker implements HandlerInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionInvoker.class);
    private final LoadingCache<InvokerKey, Method> methodInvokers;

    /**
     * Generates a new instance of ReflectionInvoker.
     */
    @Inject
    public ReflectionInvoker(final SagaAnalyzer analyzer) {
        methodInvokers = CacheBuilder.newBuilder().build(new MethodSearcher(analyzer.scanHandledMessageTypes()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Saga saga, final Object message) throws InvocationTargetException, IllegalAccessException {
        InvokerKey key = InvokerKey.create(saga, message);
        Method method = tryGetMethod(key);
        if (method != null) {
            method.invoke(saga, message);
        } else {
            LOG.warn("No method found to call. key = {}}", key);
        }
    }

    /**
     * Finds the method to invoke without causing an exception.
     */
    private Method tryGetMethod(final InvokerKey key) {
        Method methodToInvoke = null;
        try {
            methodToInvoke = methodInvokers.get(key);
        } catch (Exception ex) {
            LOG.warn("Error fetching method to invoke method {}", key, ex);
        }

        return methodToInvoke;
    }

    private static class MethodSearcher extends CacheLoader<InvokerKey, Method> {
        private final Map<Class<? extends Saga>, SagaHandlersMap> handlersMapMap;

        public MethodSearcher(final Map<Class<? extends Saga>, SagaHandlersMap> handlersMapMap) {
            this.handlersMapMap = handlersMapMap;
        }

        @Override
        public Method load(final InvokerKey key) throws Exception {
            Method methodToInvoke = null;
            SagaHandlersMap handlers = handlersMapMap.get(key.getSagaClass());
            if (handlers != null) {
                for (MessageHandler handler : handlers.messageHandlers()) {
                    if (handler.getMessageType().equals(key.getMsgClass())) {
                        methodToInvoke = handler.getMethodToInvoke();
                        break;
                    }
                }
            }

            return methodToInvoke;
        }
    }

    private static class InvokerKey {
        private final Class sagaClazz;
        private final Class msgClazz;

        public InvokerKey(final Class sagaClazz, final Class msgClazz) {
            this.sagaClazz = sagaClazz;
            this.msgClazz = msgClazz;
        }

        private Class getSagaClass() {
            return sagaClazz;
        }

        private Class getMsgClass() {
            return msgClazz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sagaClazz, msgClazz);
        }

        @Override
        public boolean equals(final Object obj) {
            boolean isEqual = false;

            if (obj != null && obj instanceof InvokerKey) {
                InvokerKey other = (InvokerKey) obj;
                isEqual = Objects.equals(msgClazz, other.msgClazz) &&
                          Objects.equals(sagaClazz, other.sagaClazz);
            }

            return isEqual;
        }

        public static InvokerKey create(Saga sagaInstance, Object msgInstance) {
            return new InvokerKey(sagaInstance.getClass(), msgInstance.getClass());
        }

        @Override
        public String toString() {
            return toStringHelper(this)
                    .add("saga", sagaClazz)
                    .add("msg", msgClazz)
                    .toString();
        }
    }
}