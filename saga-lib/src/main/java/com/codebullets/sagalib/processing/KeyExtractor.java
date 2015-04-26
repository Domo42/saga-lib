package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.context.LookupContext;
import com.codebullets.sagalib.Saga;

/**
 * Determines the saga key values of the incoming messages.
 * The saga key is used to search for existing saga states as
 * events are processed.
 */
public interface KeyExtractor {
    /**
     * Extract the instance key from the message to find the matching saga instance.
     */
    Object findSagaInstanceKey(Class<? extends Saga> sagaClazz, LookupContext context);
}