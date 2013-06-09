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
    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    /**
     * Gets the data the timeout expired is triggered.
     */
    public Date getExpiredAt() {
        return expiredAt;
    }

    /**
     * Sets the date the timeout expired is triggered.
     */
    public void setExpiredAt(Date expiredAt) {
        this.expiredAt = expiredAt;
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
