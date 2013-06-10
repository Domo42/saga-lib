package com.codebullets.sagalib.startup;

import com.codebullets.sagalib.Saga;

import java.util.Map;

/**
 * Analyzes a single saga type to determine handler methods
 * as well as supported message types.
 */
public interface SagaAnalyzer {
    /**
     * Performs a local scan on all found saga types an returns maps of handles message events
     * grouped by the saga.
     */
    Map<Class<? extends Saga>, SagaMessageMap> scanHandledMessageTypes();
}
