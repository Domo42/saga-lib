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

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Merges the result from both the annotation as well as direct
 * description saga analyzer.
 */
public class CombinedSagaAnalyzer implements SagaAnalyzer {
    private final AnnotationSagaAnalyzer annotationAnalyzer;
    private final DirectDescriptionAnalyzer directAnalyzer;

    private Map<Class<? extends Saga>, SagaHandlersMap> scanResult;

    /**
     * Creates a new instance of CombinedSagaAnalyzer.
     */
    @Inject
    public CombinedSagaAnalyzer(final AnnotationSagaAnalyzer annotationAnalyzer, final DirectDescriptionAnalyzer directAnalyzer) {
        this.annotationAnalyzer = annotationAnalyzer;
        this.directAnalyzer = directAnalyzer;
    }

    @Override
    public Map<Class<? extends Saga>, SagaHandlersMap> scanHandledMessageTypes() {
        if (scanResult == null) {
            Map<Class<? extends Saga>, SagaHandlersMap> directResults = directAnalyzer.scanHandledMessageTypes();
            Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap = annotationAnalyzer.scanHandledMessageTypes();
            scanResult = mergeResults(directResults, classSagaHandlersMapMap);
        }

        return scanResult;
    }

    private Map<Class<? extends Saga>, SagaHandlersMap> mergeResults(
            final Map<Class<? extends Saga>, SagaHandlersMap> directResults,
            final Map<Class<? extends Saga>, SagaHandlersMap> classSagaHandlersMapMap) {
        // direct reported results have preference over annotations
        Map<Class<? extends Saga>, SagaHandlersMap> mergedMap = new HashMap<>();
        mergedMap.putAll(classSagaHandlersMapMap);

        // this will overwrite values or already known keys
        mergedMap.putAll(directResults);

        return mergedMap;
    }
}
