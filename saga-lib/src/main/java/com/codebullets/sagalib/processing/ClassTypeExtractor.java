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
package com.codebullets.sagalib.processing;

import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Extract and returns all base class types as well as interfaces
 * implemented of a specific class.
 */
public class ClassTypeExtractor {
    private final List<Class<?>> classes = new ArrayList<>();
    private final Set<Class<?>> interfaces = new HashSet<>();

    /**
     * Generates a new instance of ClassTypeExtractor.
     */
    public ClassTypeExtractor(final Class<?> rootType) {
        scanType(rootType);
    }

    /**
     * Returns a list of all classes represented. This list includes
     * the root type from the constructor as well.
     */
    public Iterable<Class<?>> allClasses() {
        return classes;
    }

    /**
     * Gets a list of all implemented interfaces of the specified root type.
     */
    public Iterable<Class<?>> interfaces() {
        return interfaces;
    }

    /**
     * Gets a combined list of super classes and interfaces. The super classes
     * are always returned first.
     */
    public Iterable<Class<?>> allClassesAndInterfaces() {
        return Iterables.concat(allClasses(), interfaces());
    }

    private void scanType(final Class<?> root) {
        addAllInterfaces(root);
        classes.add(root);

        for (Class<?> superClass = root.getSuperclass(); superClass != null; superClass = superClass.getSuperclass()) {
            classes.add(superClass);
            addAllInterfaces(superClass);
        }
    }

    private void addAllInterfaces(final Class<?> root) {
        Class<?>[] classInterfaces = root.getInterfaces();

        for (Class<?> interfaceType : classInterfaces) {
            addAllInterfaces(interfaceType);
        }

        interfaces.addAll(Arrays.asList(classInterfaces));
    }
}