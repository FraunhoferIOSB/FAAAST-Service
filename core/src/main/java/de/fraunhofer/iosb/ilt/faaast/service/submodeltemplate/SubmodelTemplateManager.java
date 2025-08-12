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

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Manages all submodel template processors.
 */
public class SubmodelTemplateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelTemplateManager.class);
    private static final String VALUE_NULL = "value must not be null";
    private static final String ELEMENT_NULL = "element must not be null";

    private final List<SubmodelTemplateProcessor> submodelTemplateProcessors;
    private final Service service;
    private final List<SubscriptionId> subscriptions;

    public SubmodelTemplateManager(Service service, List<SubmodelTemplateProcessor> submodelTemplateProcessors) {
        this.service = service;
        this.submodelTemplateProcessors = submodelTemplateProcessors;
        if (submodelTemplateProcessors == null) {
            submodelTemplateProcessors = new ArrayList<>();
        }
        subscriptions = new ArrayList<>();
    }


    /**
     * Initializes the submodel template processors.
     *
     * @throws ConfigurationException if invalid configuration is provided
     * @throws PersistenceException if storage error occurs
     * @throws MessageBusException if message bus error occurs
     */
    public void init() throws ConfigurationException, PersistenceException, MessageBusException {
        ServiceConfig config = service.getConfig();
        if (config.getSubmodelTemplateProcessors() != null) {
            for (var submodelTemplateProcessorConfig: config.getSubmodelTemplateProcessors()) {
                SubmodelTemplateProcessor submodelTemplateProcessor = (SubmodelTemplateProcessor) submodelTemplateProcessorConfig.newInstance(config.getCore(), service);
                submodelTemplateProcessor.init(config.getCore(), submodelTemplateProcessorConfig, service);
                submodelTemplateProcessors.add(submodelTemplateProcessor);
            }
        }
        if (submodelTemplateProcessors.isEmpty()) {
            return;
        }
        List<Submodel> submodels = service.getPersistence().getAllSubmodels(QueryModifier.MAXIMAL, PagingInfo.ALL).getContent();
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


    private void addSubmodel(Submodel submodel) throws PersistenceException {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.add(submodel, service.getAssetConnectionManager())) {
                LOGGER.debug("addSubmodel: submodelTemplate processed successfully");
                service.getPersistence().save(submodel);
            }
        }
    }


    private void updateSubmodel(Submodel submodel) throws PersistenceException {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.update(submodel, service.getAssetConnectionManager())) {
                LOGGER.debug("updateSubmodel: submodelTemplate processed successfully");
                service.getPersistence().save(submodel);
            }
        }
    }


    private void deleteSubmodel(Submodel submodel) {
        for (var submodelTemplateProcessor: submodelTemplateProcessors) {
            if (submodelTemplateProcessor.accept(submodel) && submodelTemplateProcessor.delete(submodel, service.getAssetConnectionManager())) {
                LOGGER.debug("deleteSubmodel: submodelTemplate processed successfully");
            }
        }
    }


    private void subscribeMessageBus() throws MessageBusException {
        MessageBus messageBus = service.getMessageBus();
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementCreateEventMessage.class, this::handleCreateEvent)));
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementUpdateEventMessage.class, this::handleUpdateEvent)));
        subscriptions.add(messageBus.subscribe(SubscriptionInfo.create(ElementDeleteEventMessage.class, this::handleDeleteEvent)));
    }


    private void unsubscribeMessageBus() {
        for (var subscription: subscriptions) {
            try {
                service.getMessageBus().unsubscribe(subscription);
            }
            catch (Exception ex) {
                LOGGER.error("unsubscribeMessageBus Exception", ex);
            }
        }
        subscriptions.clear();
    }


    private void elementCreated(Referable value, Reference reference) {
        Ensure.requireNonNull(value, VALUE_NULL);

        try {
            if (value instanceof Submodel submodel) {
                addSubmodel(submodel);
            }
            else if (value instanceof SubmodelElement) {
                // if a SubmodelElement changed, we use updateSubodel
                SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(reference);
                updateSubmodel(service.getPersistence().getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
        }
        catch (Exception e) {
            LOGGER.error("elementCreated Exception", e);
        }
    }


    private void elementDeleted(Referable value, Reference reference) {
        Ensure.requireNonNull(value, ELEMENT_NULL);

        try {
            if (value instanceof Submodel submodel) {
                deleteSubmodel(submodel);
            }
            else if (value instanceof SubmodelElement) {
                // if a SubmodelElement changed, we use updateSubodel
                SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(reference);
                updateSubmodel(service.getPersistence().getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
        }
        catch (Exception e) {
            LOGGER.error("elementDeleted Exception", e);
        }
    }


    private void elementUpdated(Referable value, Reference reference) {
        Ensure.requireNonNull(value, VALUE_NULL);

        try {
            if (value instanceof Submodel submodel) {
                updateSubmodel(submodel);
            }
            else if (value instanceof SubmodelElement) {
                // if a SubmodelElement changed, we use updateSubodel
                SubmodelElementIdentifier submodelElementIdentifier = SubmodelElementIdentifier.fromReference(reference);
                updateSubmodel(service.getPersistence().getSubmodel(submodelElementIdentifier.getSubmodelId(), QueryModifier.DEFAULT));
            }
        }
        catch (Exception e) {
            LOGGER.error("elementUpdated Exception", e);
        }
    }


    /**
     * Callback message for Create event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleCreateEvent(ElementCreateEventMessage event) {
        elementCreated(event.getValue(), event.getElement());
    }


    /**
     * Callback message for Update event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleUpdateEvent(ElementUpdateEventMessage event) {
        elementUpdated(event.getValue(), event.getElement());
    }


    /**
     * Callback message for Delete event from the MessageBus.
     *
     * @param event The event from the MessageBus.
     */
    public void handleDeleteEvent(ElementDeleteEventMessage event) {
        elementDeleted(event.getValue(), event.getElement());
    }
}
