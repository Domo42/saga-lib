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

import com.codebullets.sagalib.AbstractSaga;
import com.codebullets.sagalib.AbstractSingleEventSaga;
import com.codebullets.sagalib.Saga;
import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Uses reflections library to scan for saga instances.</p>
 *
 * <p>See <a href="https://code.google.com/p/reflections/">https://code.google.com/p/reflections/</a> for details.</p>
 */
public class ReflectionsTypeScanner implements TypeScanner {
    private final Reflections reflections;

    /**
     * Generates a new instance of ReflectionsTypeScanner.
     */
    public ReflectionsTypeScanner() {
        reflections = new Reflections(ClasspathHelper.forJavaClassPath());
    }

    /**
     * Generates a new instance of ReflectionsTypeScanner. Allows
     * configuration of custom Reflections settings.
     */
    public ReflectionsTypeScanner(final Reflections reflections) {
        this.reflections = reflections;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Class<? extends Saga>> scanForSagas() {
        Set<Class<? extends Saga>> sagaTypes = reflections.getSubTypesOf(Saga.class);

        // separate searches in case saga-lib is in embedded jar when performing directory scanning
        Set<Class<? extends AbstractSaga>> abstractSagaTypes = reflections.getSubTypesOf(AbstractSaga.class);
        Set<Class<? extends AbstractSingleEventSaga>> singleEventSagaTypes = reflections.getSubTypesOf(AbstractSingleEventSaga.class);

        Set<Class<? extends Saga>> foundTypes = Sets.union(sagaTypes, abstractSagaTypes);
        Set<Class<? extends Saga>> mergedSet = Sets.union(foundTypes, copy(singleEventSagaTypes));

        return removeAbstractTypes(mergedSet);
    }

    /**
     * Creates a new collection with abstract types which can not be instantiated.
     */
    private Collection<Class<? extends Saga>> removeAbstractTypes(final Collection<Class<? extends Saga>> foundTypes) {
        Collection<Class<? extends Saga>> sagaTypes = new ArrayList<>();

        for (Class<? extends Saga> entryType : foundTypes) {
            if (!Modifier.isAbstract(entryType.getModifiers())) {
                sagaTypes.add(entryType);
            }
        }

        return sagaTypes;
    }

    private Set<Class<? extends Saga>> copy(final Set<Class<? extends AbstractSingleEventSaga>> source) {
        Set<Class<? extends Saga>> target = new HashSet<>();
        for (Class<? extends Saga> entry : source) {
            target.add(entry);
        }

        return target;
    }
}