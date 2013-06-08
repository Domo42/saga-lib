package com.codebullets.sagalib;

/**
 * Contains all the state of a sage. This state is stored and
 * loaded by this library. It is automatically attached to a saga
 * as a matching message arrives.
 */
public interface SagaState {

    /**
     * Gets the identifier of the saga. The id is generated automatically
     * as a new saga and saga state is created.
     */
    public String getSagaId();
}
