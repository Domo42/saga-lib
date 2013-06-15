package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.Saga;
import com.codebullets.sagalib.SagaState;
import com.codebullets.sagalib.startup.MessageHandler;
import com.codebullets.sagalib.startup.SagaAnalyzer;
import com.codebullets.sagalib.startup.SagaHandlersMap;
import com.codebullets.sagalib.storage.StateStorage;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible to create new instances of sagas.
 */
public class SagaFactory {
    private static final Logger LOG = LoggerFactory.getLogger(SagaFactory.class);
    private final Multimap<Class, Class<? extends Saga>> messagesToContinueSaga = HashMultimap.create();
    private final Multimap<Class, Class<? extends Saga>> messagesStartingSagas = HashMultimap.create();
    private final LoadingCache<Class<? extends Saga>, Provider<? extends Saga>> providers;
    private KeyExtractor keyExtractor;
    private StateStorage stateStorage;

    /**
     * Generates a new instance of SagaFactory.
     */
    @Inject
    public SagaFactory(final SagaAnalyzer sagaAnalyzer, final SagaProviderFactory providerFactory, final KeyExtractor keyExtractor,
                       final StateStorage stateStorage) {
        this.keyExtractor = keyExtractor;
        this.stateStorage = stateStorage;
        // Create providers when needed. Cache providers for later use.
        providers = CacheBuilder.newBuilder().build(new ProviderLoader(providerFactory));

        // scan for sagas and their messages being handled
        Map<Class<? extends Saga>, SagaHandlersMap> handlersMap = sagaAnalyzer.scanHandledMessageTypes();
        initializeMessageMappings(handlersMap);
    }

    /**
     * Creates new instances based on the message type provided.
     * @return Returns a saga instance.
     */
    public Collection<Saga> create(final Object message) {
        Collection<Saga> sagaInstances = new ArrayList<>();

        // create and start a new saga if message has been flagged as such
        Collection<Class<? extends Saga>> startingSagaTypes = messagesStartingSagas.get(message.getClass());
        for (Class<? extends Saga> sagaType : startingSagaTypes) {
            sagaInstances.add(startNewSaga(sagaType));
        }

        // Search for existing saga states and attach them to created instances.
        Collection <Class<? extends Saga>> existingSagaTypes = messagesToContinueSaga.get(message.getClass());
        for (Class<? extends Saga> sagaType : existingSagaTypes) {
            Collection<Saga> sagas = continueSagas(sagaType, message);
            sagaInstances.addAll(sagas);
        }

        return sagaInstances;
    }

    /**
     * Search for existing saga states and attach them to saga instances.
     */
    private Collection<Saga> continueSagas(final Class<? extends Saga> sagaToContinue, final Object message) {
        Collection<Saga> sagas = new ArrayList<>();

        String key = keyExtractor.findSagaInstanceKey(sagaToContinue, message);
        if (key != null) {
            Collection<? extends SagaState> sagaStates = stateStorage.load(sagaToContinue.getName(), key);
            for (SagaState sagaState : sagaStates) {
                Saga saga = continueSaga(sagaToContinue, sagaState);
                if (saga != null) {
                    sagas.add(saga);
                }
            }
        } else {
            LOG.error("Can not determine saga instance key from message {}", message);
        }

        return sagas;
    }

    /**
     * Creates a new saga instance and attaches the existing saga state.
     */
    private Saga continueSaga(final Class<? extends Saga> sagaToContinue, SagaState existingSate) {
        Saga saga = null;
        try {
            saga = providers.get(sagaToContinue).get();
            saga.setState(existingSate);
        } catch (Exception ex) {
            LOG.error("Unable to create new instance of saga type {}.", sagaToContinue, ex);
        }

        return saga;
    }

    /**
     * Starts a new saga by creating an instance and attaching a new saga state.
     */
    private Saga startNewSaga(final Class<? extends Saga> sagaToStart) {
        Saga createdSaga = null;

        try {
            Provider<? extends Saga> sagaProvider = providers.get(sagaToStart);
            createdSaga = sagaProvider.get();
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
     * Populate internal map to translate between incoming message event type and saga type.
     */
    private void initializeMessageMappings(final Map<Class<? extends Saga>, SagaHandlersMap> handlersMap) {
        for (Map.Entry<Class<? extends Saga>, SagaHandlersMap> entry : handlersMap.entrySet()) {
            Class<? extends Saga> sagaClass = entry.getKey();

            Collection<MessageHandler> sagaHandlers = entry.getValue().messageHandlers();
            for (MessageHandler handler : sagaHandlers) {

                // remember all message types where a completely new saga needs to be started.
                if (handler.getStartsSaga()) {
                    messagesStartingSagas.put(handler.getMessageType(), sagaClass);
                } else {
                    messagesToContinueSaga.put(handler.getMessageType(), sagaClass);
                }
            }
        }
    }

    /**
     * Creates a new provider on demand.
     */
    private static class ProviderLoader extends CacheLoader<Class<? extends Saga>, Provider<? extends Saga>> {
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