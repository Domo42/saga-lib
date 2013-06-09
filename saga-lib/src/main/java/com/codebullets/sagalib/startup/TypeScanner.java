package com.codebullets.sagalib.startup;

import java.util.Collection;

/**
 * Scans the current environment and returns a list of available saga types.
 */
public interface TypeScanner {
    /**
     * Perform a scan for saga types and returns all non abstract types.
     */
    Collection<Class> scanForSagas();
}
