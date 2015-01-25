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
package com.codebullets.sagalib;

import javax.annotation.Nullable;

/**
 * Extract an instance key of a specific type for a specific message.
 * @param <MESSAGE> The type of the message to extract the key from.
 * @param <KEY> The type of the key returned after extraction.
 */
public interface KeyExtractFunction<MESSAGE, KEY> {
    /**
     * Returns the key value to identify a running saga.
     */
    @Nullable
    KEY key(MESSAGE message);
}