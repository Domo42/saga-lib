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
package com.codebullets.sagalib.processing.invocation;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.describe.DescribesHandlers;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.google.common.base.MoreObjects;
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
import java.util.Optional;

/**
 * Invokes the target handler on the provided saga for the given handler.
 */
public class ReflectionInvoker implements HandlerInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(ReflectionInvoker.class);
    private final LoadingCache<InvokerKey, InvocationMethod> invocationMethods;

    /**
     * Generates a new instance of ReflectionInvoker.
     */
    @Inject
    public ReflectionInvoker(final SagaAnalyzer analyzer) {
        invocationMethods = CacheBuilder.newBuilder().build(new MethodSearcher(analyzer.scanHandledMessageTypes()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Saga saga, final Object message) {
    }

    @Override
    public void invoke(final InvocationContext invocationContext) throws InvocationTargetException, IllegalAccessException {
        Saga<?> saga = invocationContext.saga();

        if (saga instanceof DescribesHandlers) {
            ((DescribesHandlers) saga).describeHandlers().handler().accept(invocationContext.message());
        } else {
            invokeUsingReflection(invocationContext);
        }
    }

    private void invokeUsingReflection(final InvocationContext invocationContext) throws InvocationTargetException, IllegalAccessException {
        InvokerKey key = InvokerKey.create(invocationContext);
        InvocationMethod method = tryGetMethod(key);
        if (method != null) {
            Optional<Method> reflectionMethod = method.invocationMethod();
            if (reflectionMethod.isPresent()) {
                reflectionMethod.get().invoke(invocationContext.saga(), invocationContext.message());
            } else {
                LOG.warn("No annotated handler method found and saga is not self describing. key = {}", key);
            }
        } else {
            LOG.warn("No annotated handler method found and saga is not self describing. key = {}", key);
        }
    }

    /**
     * Finds the method to invoke without causing an exception.
     */
    private InvocationMethod tryGetMethod(final InvokerKey key) {
        InvocationMethod invocationMethod = null;
        try {
            invocationMethod = invocationMethods.get(key);
        } catch (Exception ex) {
            LOG.warn("Error fetching method to invoke method {}", key, ex);
        }

        return invocationMethod;
    }

    /**
     * Searches for a specific handler method in the scanned map of handlers.
     */
    private static class MethodSearcher extends CacheLoader<InvokerKey, InvocationMethod> {
        private final Map<Class<? extends Saga>, SagaHandlersMap> handlersMapMap;

        MethodSearcher(final Map<Class<? extends Saga>, SagaHandlersMap> handlersMapMap) {
            this.handlersMapMap = handlersMapMap;
        }

        @Override
        public InvocationMethod load(final InvokerKey key) {
            InvocationMethod invocationMethod = null;
            SagaHandlersMap handlers = handlersMapMap.get(key.getSagaClass());
            if (handlers != null) {
                for (MessageHandler handler : handlers.messageHandlers()) {
                    if (matchesCallingContext(key, handler) && handler.getMessageType().isAssignableFrom(key.getMsgClass())) {
                        final Optional<Method> method = handler.getMethodToInvoke();
                        invocationMethod = method.map(InvocationMethod::reflectionInvoked)
                                .orElse(InvocationMethod.selfDescribed());
                        break;
                    }
                }
            }

            return invocationMethod;
        }

        private boolean matchesCallingContext(final InvokerKey invokerKey, final MessageHandler handler) {
            boolean isMatch;

            if (invokerKey.handlerType == InvocationHandlerType.START) {
                isMatch = handler.getStartsSaga();
            } else {
                isMatch = !handler.getStartsSaga();
            }

            return isMatch;
        }
    }

    /**
     * Combined key of saga type and message being handled.
     */
    private static class InvokerKey {
        private final Class sagaClazz;
        private final Class msgClazz;
        private final InvocationHandlerType handlerType;

        InvokerKey(final Class sagaClazz, final Class msgClazz, final InvocationHandlerType handlerType) {
            this.sagaClazz = sagaClazz;
            this.msgClazz = msgClazz;
            this.handlerType = handlerType;
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

            if (obj instanceof InvokerKey) {
                InvokerKey other = (InvokerKey) obj;
                isEqual = Objects.equals(msgClazz, other.msgClazz)
                       && Objects.equals(sagaClazz, other.sagaClazz)
                       && Objects.equals(this.handlerType, other.handlerType);
            }

            return isEqual;
        }

        public static InvokerKey create(final InvocationContext invocationContext) {
            return new InvokerKey(invocationContext.saga().getClass(), invocationContext.message().getClass(), invocationContext.handlerType());
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("saga", sagaClazz)
                    .add("msg", msgClazz)
                    .toString();
        }
    }
}