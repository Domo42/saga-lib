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
package com.codebullets.sagalib.messages;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by the saga lib if a saga has requested a timeout and
 * the defined time has expired.
 */
public class Timeout implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Date expiredAt;
    private String name;

    /**
     * Gets the id of the matching saga.
     */
    public String getSagaId() {
        return sagaId;
    }

    /**
     * Sets the id of the saga.
     */
    public void setSagaId(final String sagaId) {
        this.sagaId = sagaId;
    }

    /**
     * Gets the data the timeout expired is triggered.
     */
    public Date getExpiredAt() {
        return expiredAt != null ? new Date(expiredAt.getTime()) : null;
    }

    /**
     * Sets the date the timeout expired is triggered.
     */
    public void setExpiredAt(final Date expiredAt) {
        this.expiredAt = expiredAt != null ? new Date(expiredAt.getTime()) : null;
    }

    /**
     * Gets an optional name of the timeout.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets an optional name of the timeout.
     */
    public void setName(String name) {
        this.name = name;
    }
}
