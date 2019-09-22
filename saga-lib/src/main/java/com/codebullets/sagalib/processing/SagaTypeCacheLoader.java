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
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Creates a list of saga handlers based on requested message type.
 */
class SagaTypeCacheLoader extends CacheLoader<Class, Collection<SagaType>> {
    private final Multimap<Class, SagaType> scannedTypes;
    private Collection<Class<? extends Saga>> preferredOrder = new ArrayList<>(0);

    /**
     * Generates a new instance of SagaTypeCacheLoader.
     */
    SagaTypeCacheLoader(final Multimap<Class, SagaType> scannedTypes) {
        this.scannedTypes = scannedTypes;
    }

    /**
     * Sets the handlers that should be executed first.
     */
    public void setPreferredOrder(final Collection<Class<? extends Saga>> preferredOrder) {
        this.preferredOrder = preferredOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<SagaType> load(final Class messageClass) throws Exception {
        Iterable<Class<?>> matchingTypes = allMessageTypes(messageClass);
        Collection<SagaType> matchingSagas = findAllSagas(matchingTypes);

        Collection<SagaType> resultList;
        if (matchingSagas.isEmpty()) {
            resultList = Collections.emptyList();
        } else {
            resultList = sortAccordingToPreference(matchingSagas);
        }

        return resultList;
    }

    private Collection<SagaType> sortAccordingToPreference(final Iterable<SagaType> unsorted) {
        Collection<SagaType> sorted = new LinkedList<>();

        // place preferred items first
        for (Class preferredClass : preferredOrder) {
            SagaType containedItem = containsItem(unsorted, preferredClass);
            if (containedItem != null) {
                sorted.add(containedItem);
            }
        }

        // add all starting saga types next
        for (SagaType sagaType : unsorted) {
            if (sagaType.isStartingNewSaga() && !Iterables.contains(sorted, sagaType)) {
                sorted.add(sagaType);
            }
        }

        // add all the rest not yet in sorted list.
        for (SagaType sagaType : unsorted) {
            if (!Iterables.contains(sorted, sagaType)) {
                sorted.add(sagaType);
            }
        }

        return sorted;
    }

    private Collection<SagaType> findAllSagas(final Iterable<Class<?>> messageTypes) {
        Collection<SagaType> types = new HashSet<>();

        for (Class<?> msgType : messageTypes) {
            Collection<SagaType> sagas = scannedTypes.get(msgType);
            types.addAll(sagas);
        }

        return types;
    }

    /**
     * Creates a list of types the concrete class may have handler matches. Like the
     * implemented interfaces of base classes.
     */
    private Iterable<Class<?>> allMessageTypes(final Class<?> concreteMsgClass) {
        ClassTypeExtractor extractor = new ClassTypeExtractor(concreteMsgClass);
        return extractor.allClassesAndInterfaces();
    }

    /**
     * Checks whether the source list contains a saga type matching the input class.
     */
    private SagaType containsItem(final Iterable<SagaType> source, final Class itemToSearch) {
        SagaType containedItem = null;

        for (SagaType sagaType : source) {
            if (sagaType.getSagaClass().equals(itemToSearch)) {
                containedItem = sagaType;
                break;
            }
        }

        return containedItem;
    }
}