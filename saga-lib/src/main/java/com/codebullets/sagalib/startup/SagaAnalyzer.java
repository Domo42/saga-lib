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

import com.codebullets.sagalib.Saga;

import java.util.Map;

/**
 * Analyzes a single saga type to determine handler methods
 * as well as supported message types.
 */
public interface SagaAnalyzer {
    /**
     * Performs a local scan on all found saga types an returns maps of handled message events
     * grouped by the saga.
     */
    Map<Class<? extends Saga>, SagaHandlersMap> scanHandledMessageTypes();
}