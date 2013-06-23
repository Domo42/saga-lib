package com.codebullets.sagalib.timeout;

import java.util.Date;

/**
 * Represents a clock in the system. Capable of telling the current time.
 */
public interface Clock {
    /**
     * Returns the current date and time.
     */
    Date now();
}