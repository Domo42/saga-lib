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

package com.codebullets.sagalib;

import com.codebullets.sagalib.annotations.AnnotationHandler;
import com.codebullets.sagalib.annotations.AnnotationSaga;
import com.codebullets.sagalib.description.DescriptionHandler;
import com.codebullets.sagalib.description.DescriptionSaga;
import com.codebullets.sagalib.processing.SagaProviderFactory;
import com.codebullets.sagalib.startup.EventStreamBuilder;
import com.codebullets.sagalib.startup.TypeScanner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;

import javax.inject.Provider;
import java.lang.reflect.InvocationTargetException;

public class SagaLibStream implements AutoCloseable {
    private final MessageStream messageStream;

    public SagaLibStream() {
        messageStream = EventStreamBuilder.configure()
                .usingScanner(createConstantTypeScanner())
                .usingSagaProviderFactory(providerFactory())
                .build();
    }

    @Override
    public void close() {
        AutoCloseables.closeQuietly(messageStream);
    }

    public void handle(final Object message) {
        try {
            messageStream.handle(message);
        } catch (InvocationTargetException | IllegalAccessException e) {
            System.out.println("Error handling message." + e);
            Throwables.propagate(e);
        }
    }

    private SagaProviderFactory providerFactory() {
        return new SagaProviderFactory() {
            @Override
            public <T extends Saga> Provider<T> createProvider(final Class<T> sagaClass) {
                Provider<T> provider = null;

                if (sagaClass.equals(DescriptionHandler.class)) {
                    provider = (Provider) () -> new DescriptionHandler();
                } else if (sagaClass.equals(AnnotationHandler.class)) {
                    provider = (Provider) () -> new AnnotationHandler();
                } else if (sagaClass.equals(DescriptionSaga.class)) {
                    provider = (Provider) () -> new DescriptionSaga();
                } else if (sagaClass.equals(AnnotationSaga.class)) {
                    provider = (Provider) () -> new AnnotationSaga();
                }

                return provider;
            }
        };
    }

    private TypeScanner createConstantTypeScanner() {
        return () -> ImmutableSet.of(
                AnnotationHandler.class,
                DescriptionHandler.class,
                AnnotationSaga.class,
                DescriptionSaga.class);
    }
}
