package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.storage.StateStorage;
import com.codebullets.sagalib.timeout.NeedTimeouts;
import com.codebullets.sagalib.timeout.TimeoutManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * Responsible to create new instances of sagas.
 */
@SuppressWarnings("unchecked")
public class SagaFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SagaFactory.class);
    private final LoadingCache<Class<? extends Saga>, Provider<? extends Saga>> providers;
    private final TimeoutManager timeoutManager;
    private final Organizer organizer;
    private StateStorage stateStorage;

    /**
     * Generates a new instance of SagaFactory.
     */
    @Inject
    public SagaFactory(final SagaProviderFactory providerFactory,
                       final StateStorage stateStorage,
                       final TimeoutManager timeoutManager,
                       final Organizer organizer) {
        this.stateStorage = stateStorage;
        this.timeoutManager = timeoutManager;
        this.organizer = organizer;

        // Create providers when needed. Cache providers for later use.
        providers = CacheBuilder.newBuilder().build(new ProviderLoader(providerFactory));
    }

    /**
     * Creates new instances based on the message type provided.
     * @return Returns a saga instance.
     */
    public Collection<SagaInstanceDescription> create(final Object message) {
        Collection<SagaInstanceDescription> sagaInstances = new ArrayList<>();

        for (SagaType sagaType : organizer.sagaTypesForMessage(message)) {
            if (sagaType.isStartingNewSaga()) {
                Saga newSaga = startNewSaga(sagaType.getSagaClass());
                sagaInstances.add(SagaInstanceDescription.define(newSaga, true));
            } else {
                Collection<Saga> sagas = continueExistingSaga(sagaType);
                for (Saga saga : sagas) {
                    sagaInstances.add(SagaInstanceDescription.define(saga, false));
                }
            }
        }

        return sagaInstances;
    }

    /**
     * Create a new saga instance with already existing saga state.
     */
    private Collection<Saga> continueExistingSaga(final SagaType sagaType) {
        Collection<Saga> sagas = new ArrayList<>();

        String sagaId = sagaType.getSagaId();
        if (sagaId != null) {
            // saga id is known -> we can create saga directly from know state.
            Saga saga = createSagaBasedOnId(sagaId);
            sagas.add(saga);
        } else {
            // no saga id available, search for existing state based on instance key.
            Collection<Saga> existingSagas = continueSagas(sagaType);
            sagas.addAll(existingSagas);
        }

        return sagas;
    }

    private Saga createSagaBasedOnId(final String sagaId) {
        Saga sagaToContinue = null;

        // saga id is know -> we can create saga directly from know state.
        SagaState state = stateStorage.load(sagaId);
        if (state != null) {
            sagaToContinue = continueSaga(state.getType(), state);
        } else {
            LOG.warn("No open saga state found. saga id = {}", sagaId);
        }

        return sagaToContinue;
    }

    /**
     * Search for existing saga states and attach them to saga instances.
     */
    private Collection<Saga> continueSagas(final SagaType sagaType) {
        Collection<Saga> sagas = new ArrayList<>();

        Collection<? extends SagaState> sagaStates = stateStorage.load(sagaType.getSagaClass().getName(), sagaType.getInstanceKey());
        for (SagaState sagaState : sagaStates) {
            Saga saga = continueSaga(sagaType.getSagaClass(), sagaState);
            if (saga != null) {
                sagas.add(saga);
            }
        }

        return sagas;
    }

    /**
     * Creates a new saga instance and attaches the existing saga state.
     */
    private Saga continueSaga(final Class<? extends Saga> sagaToContinue, final SagaState existingSate) {
        Saga saga;

        try {
            saga = createNewSagaInstance(sagaToContinue);
            saga.setState(existingSate);
        } catch (Exception ex) {
            saga = null;
            LOG.error("Unable to create new instance of saga type {}.", sagaToContinue, ex);
        }

        return saga;
    }

    /**
     * Create a new saga instance based on fully qualified name and the existing saga state.
     */
    private Saga continueSaga(final String sagaToContinue, final SagaState existingState) {
        Saga saga = null;

        try {
            Class clazz = Class.forName(sagaToContinue);
            saga = continueSaga(clazz, existingState);
        } catch (Exception ex) {
            LOG.error("Error creating instance for saga type string {}", sagaToContinue);
        }

        return saga;
    }

    /**
     * Starts a new saga by creating an instance and attaching a new saga state.
     */
    private Saga startNewSaga(final Class<? extends Saga> sagaToStart) {
        Saga createdSaga = null;

        try {
            createdSaga = createNewSagaInstance(sagaToStart);
            createdSaga.createNewState();

            SagaState newState = createdSaga.state();
            newState.setSagaId(UUID.randomUUID().toString());
            newState.setType(sagaToStart.getName());
        } catch (Exception ex) {
            LOG.error("Unable to create new instance of saga type {}.", sagaToStart, ex);
        }

        return createdSaga;
    }

    /**
     * Creates a new saga instances with the requested type.
     * @throws Exception Forwards possible exceptions from the provider get method.
     */
    private Saga createNewSagaInstance(final Class<? extends Saga> sagaType) throws Exception {
        Provider<? extends Saga> sagaProvider = providers.get(sagaType);
        Saga createdSaga = sagaProvider.get();
        if (createdSaga instanceof NeedTimeouts) {
            ((NeedTimeouts) createdSaga).setTimeoutManager(timeoutManager);
        }

        return createdSaga;
    }

    /**
     * Creates a new provider on demand.
     */
    private final static class ProviderLoader extends CacheLoader<Class<? extends Saga>, Provider<? extends Saga>> {
        private final SagaProviderFactory providerFactory;

        private ProviderLoader(final SagaProviderFactory providerFactory) {
            this.providerFactory = providerFactory;
        }

        @Override
        public Provider<? extends Saga> load(final Class<? extends Saga> key) throws Exception {
            return providerFactory.createProvider(key);
        }
    }
}