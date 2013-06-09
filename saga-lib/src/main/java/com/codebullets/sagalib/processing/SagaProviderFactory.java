package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;

import javax.inject.Provider;

/**
 * When asked returns a provider capable of create a new saga instance.
 */
public interface SagaProviderFactory {
    /**
     * Creates a new provider capable of creating a new instance of the class
     * specified in the parameter.
     */
    Provider<Saga> createProvider(Class sagaClass);
}
