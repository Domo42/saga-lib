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

    /**
     * Gets a string identifying one specific type of saga.
     */
    public String getType();

    /**
     * Gets a string to identify a saga in combination with the type. For example a request id
     * from the external API. Any kind of id tied the saga type and messages expected. Can be changed
     * as the events are handled by the saga.
     */
    public String instanceId();
}
