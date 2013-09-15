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
package com.codebullets.sagalib.context;

/**
 * When implemented by a saga the saga will the setter of the execution
 * context will be called before any of the saga handlers is executed.<p/>
 * The {@link com.codebullets.sagalib.AbstractSaga} as well as {@link com.codebullets.sagalib.AbstractSingleEventSaga}
 * class already implement this interface.
 */
public interface NeedContext {
    /**
     * Sets the current message execution context.
     */
    void setExecutionContext(ExecutionContext context);
}