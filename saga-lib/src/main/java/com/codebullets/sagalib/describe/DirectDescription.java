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
package com.codebullets.sagalib.describe;

/**
 * Indicates that the saga describes the messages handled and
 * handlers by itself without relying on the annotation scanning
 * of the saga-lib.
 */
public interface DirectDescription {
    /**
     * Describe one self about the messages handled and executed.
     *
     * <p>ATTENTION: Do not cache the description, when referencing instance methods.
     * The saga-lib creates new instances of sagas all the time to prevent concurrent
     * access from possible multiple threads. When cached methods might be called on
     * a different saga instance originally intended.
     * </p>
     */
    SagaDescription describe();
}
