/*
 * Copyright 2016 Stefan Domnanovits
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

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.describe.DescribesHandlers;
import com.codebullets.sagalib.describe.HandlerDescription;
import com.codebullets.sagalib.processing.SagaInstanceCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

/**
 * Analyzes saga types based on the fact whether they implement the
 * {@link DescribesHandlers} interface.
 */
public class HandlerDescriptionAnalyzer implements SagaAnalyzer {
    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSagaAnalyzer.class);

    private final TypeScanner typeScanner;
    private final SagaInstanceCreator instanceCreator;

    /**
     * Creates a new HandlerDescriptionAnalyzer instance.
     */
    @Inject
    public HandlerDescriptionAnalyzer(final TypeScanner typeScanner, final SagaInstanceCreator instanceCreator) {
        this.typeScanner = typeScanner;
        this.instanceCreator = instanceCreator;
    }

    @Override
    public Map<Class<? extends Saga>, SagaHandlersMap> scanHandledMessageTypes() {

        Collection<Class<? extends Saga>> sagaClasses = typeScanner.scanForSagas();
        Stream<DescribesHandlers> directDescriptionSagas = sagaClasses.stream()
                .filter(DescribesHandlers.class::isAssignableFrom)
                .filter(c -> !Modifier.isAbstract(c.getModifiers()))
                .flatMap(this::createDirectDescriptionInstance);

        return createHandlersMap(directDescriptionSagas);
    }

    private Map<Class<? extends Saga>, SagaHandlersMap> createHandlersMap(final Stream<DescribesHandlers> directDescriptionSagas) {
        Map<Class<? extends Saga>, SagaHandlersMap> handlersMap = new HashMap<>();

        directDescriptionSagas.forEach(saga -> {
            Class<? extends Saga> sagaType = (Class<? extends Saga>) saga.getClass();
            HandlerDescription handlerDescription = saga.describeHandlers();
            SagaHandlersMap handlers = new SagaHandlersMap(sagaType);
            final Class<?> startedBy = handlerDescription.startedBy();

            handlerDescription.handlerTypes().forEach(handlerType -> {
                handlers.add(MessageHandler.selfDescribedHandler(handlerType, handlerType.equals(startedBy)));
            });

            handlersMap.put(sagaType, handlers);
        });

        return handlersMap;
    }

    private Stream<DescribesHandlers> createDirectDescriptionInstance(final Class<? extends Saga> clazz) {
        DescribesHandlers newInstance = null;
        try {
            newInstance = (DescribesHandlers) instanceCreator.createNew(clazz);
        } catch (ExecutionException e) {
            LOG.error("Error creating saga instance of type {} to read description.", clazz, e.getCause());
        } catch (Exception e) {
            LOG.error("Error creating saga instance of type {} to read description.", clazz, e);
        }

        return newInstance != null ? Stream.of(newInstance) : Stream.empty();
    }
}
