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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelElementByPathResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class PutSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PutSubmodelElementByPathRequest, PutSubmodelElementByPathResponse> {

    public PutSubmodelElementByPathRequestHandler(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PutSubmodelElementByPathResponse doProcess(PutSubmodelElementByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ValidationException {
        ModelValidator.validate(request.getSubmodelElement(), coreConfig.getValidationOnUpdate());
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getSubmodelId(), Submodel.class);
        //Check if submodelelement does exist
        SubmodelElement currentSubmodelElement = persistence.get(reference, QueryModifier.DEFAULT);
        SubmodelElement newSubmodelElement = request.getSubmodelElement();
        if (ElementValueHelper.isSerializableAsValue(currentSubmodelElement.getClass())) {
            ElementValue oldValue = ElementValueMapper.toValue(currentSubmodelElement);
            ElementValue newValue = ElementValueMapper.toValue(newSubmodelElement);
            if (!Objects.equals(oldValue, newValue)) {
                assetConnectionManager.setValue(reference, newValue);
                messageBus.publish(ValueChangeEventMessage.builder()
                        .element(reference)
                        .oldValue(oldValue)
                        .newValue(newValue)
                        .build());
            }
        }
        currentSubmodelElement = persistence.put(null, reference, newSubmodelElement);
        messageBus.publish(ElementUpdateEventMessage.builder()
                .element(reference)
                .value(currentSubmodelElement)
                .build());
        return PutSubmodelElementByPathResponse.builder()
                .payload(currentSubmodelElement)
                .success()
                .build();
    }
}
