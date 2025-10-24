/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages all submodel template processors.
 */
public class SubmodelTemplateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelTemplateManager.class);

    private final Persistence persistence;
    private final MessageBus messageBus;
    private final AssetConnectionManager assetConnectionManager;
    private final List<SubscriptionId> subscriptions = new ArrayList<>();
    private List<SubmodelTemplateProcessor> submodelTemplateProcessors = new ArrayList<>();

    public SubmodelTemplateManager(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager,
            List<SubmodelTemplateProcessor> submodelTemplateProcessors) {
        Ensure.requireNonNull(submodelTemplateProcessors, "submodelTemplateProcessors must be non-null");
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
        this.submodelTemplateProcessors = submodelTemplateProcessors;
    }


    /**
     * Starts the processing submodel templates.
     *
     * @throws PersistenceException if storage error occurs
     * @throws MessageBusException if message bus error occurs
     */
    public void start() throws PersistenceException, MessageBusException {
        if (submodelTemplateProcessors.isEmpty()) {
            return;
        }
        List<Submodel> submodels = persistence.getAllSubmodels(QueryModifier.MAXIMAL, PagingInfo.ALL).getContent();
        for (var submodel: submodels) {
            addSubmodel(submodel);
        }
        subscribeMessageBus();
    }


    /**
     * Stops all submodel template processors.
     */
    public void stop() {
        unsubscribeMessageBus();
    }


    /**
     * Callback message for Create event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleCreateEvent(ElementCreateEventMessage event) {
        if (event.getValue() instanceof Submodel submodel) {
            addSubmodel(submodel);
        }
        else if (event.getValue() instanceof SubmodelElement) {
            // if a SubmodelElement changed, we use updateSubodel
            SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(event.getElement());
            try {
                updateSubmodel(persistence.getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
            catch (ResourceNotFoundException | PersistenceException e) {
                LOGGER.warn("Failed to read submodel (submodelId: {})", submodelElementIdentifier.getSubmodelId(), e);
            }
        }
    }


    /**
     * Callback message for Update event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleUpdateEvent(ElementUpdateEventMessage event) {
        if (event.getValue() instanceof Submodel submodel) {
            updateSubmodel(submodel);
        }
        else if (event.getValue() instanceof SubmodelElement) {
            // if a SubmodelElement changed, we use updateSubodel
            SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(event.getElement());
            try {
                updateSubmodel(persistence.getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
            catch (ResourceNotFoundException | PersistenceException e) {
                LOGGER.warn("Failed to read submodel (submodelId: {})", submodelElementIdentifier.getSubmodelId(), e);
            }
        }
    }


    /**
     * Callback message for Delete event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleDeleteEvent(ElementDeleteEventMessage event) {
        if (event.getValue() instanceof Submodel submodel) {
            deleteSubmodel(submodel);
        }
        else if (event.getValue() instanceof SubmodelElement) {
            // if a SubmodelElement changed, we use updateSubodel
            SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(event.getElement());
            try {
                updateSubmodel(persistence.getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
            catch (ResourceNotFoundException | PersistenceException e) {
                LOGGER.warn("Failed to read submodel (submodelId: {})", submodelElementIdentifier.getSubmodelId(), e);
            }
        }
    }


    private void addSubmodel(Submodel submodel) {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.add(submodel, assetConnectionManager)) {
                LOGGER.debug("addSubmodel: submodelTemplate processed successfully");
                try {
                    persistence.save(submodel);
                }
                catch (PersistenceException e) {
                    LOGGER.warn("Failed to save submodel added by SMT processor (submodelId: {}, SMT processor type: {})",
                            submodel.getId(),
                            submodelTemplateProcessor.getClass().getSimpleName(),
                            e);
                }
            }
        }
    }


    private void updateSubmodel(Submodel submodel) {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.update(submodel, assetConnectionManager)) {
                LOGGER.debug("updateSubmodel: submodelTemplate processed successfully");
                try {
                    persistence.save(submodel);
                }
                catch (PersistenceException e) {
                    LOGGER.warn("Failed to save submodel updated by SMT processor (submodelId: {}, SMT processor type: {})",
                            submodel.getId(),
                            submodelTemplateProcessor.getClass().getSimpleName(),
                            e);
                }
            }
        }
    }


    private void deleteSubmodel(Submodel submodel) {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.delete(submodel, assetConnectionManager)) {
                LOGGER.debug("deleteSubmodel: submodelTemplate processed successfully");
            }
        }
    }


    private void subscribeMessageBus() throws MessageBusException {
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, this::handleCreateEvent)));
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, this::handleUpdateEvent)));
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, this::handleDeleteEvent)));
        // ValueChangeEventMessage
    }


    private void unsubscribeMessageBus() {
        for (var subscription: subscriptions) {
            try {
                messageBus.unsubscribe(subscription);
            }
            catch (MessageBusException e) {
                LOGGER.warn("failed to unsubscribe from message bus (subscriptionId: {})", subscription.getValue(), e);
            }
        }
        subscriptions.clear();
    }
}
