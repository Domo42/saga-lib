package com.codebullets.sagalib.storage;

import com.codebullets.sagalib.SagaState;

import java.util.Collection;

/**
 * Saves and loads the state of a saga.
 */
public interface StateStorage {
    /**
     * Save the state so that in can be retrieved later on by the {@link #load(String)} method.
     * @param state The saga state to save.
     */
    void save(SagaState state);

    /**
     * Load a single saga state based on unique saga id.
     * @return Returns the available saga instance. Returns null if nothing has been found.
     */
    SagaState load(String sagaId);

    /**
     * Delete the state of
     * @param sagaId
     */
    void delete(String sagaId);

    /**
     * Load a list of saga states based on type and instance id. The instance id is chosen
     * by the saga implementer and should in ideal cases only return on instance inside the collection.
     *
     * @return List of found saga state instances.
     */
    Collection<SagaState> load(String type, String instanceId);
}
