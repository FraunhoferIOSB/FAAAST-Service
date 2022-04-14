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
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.Objects;
import java.util.Optional;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelElementByPathRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelElementByPathResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GetSubmodelElementByPathRequestHandler extends RequestHandler<GetSubmodelElementByPathRequest, GetSubmodelElementByPathResponse> {

    public GetSubmodelElementByPathRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetSubmodelElementByPathResponse process(GetSubmodelElementByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException {
        GetSubmodelElementByPathResponse response = new GetSubmodelElementByPathResponse();
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getId(), Submodel.class);
        SubmodelElement submodelElement = persistence.get(reference, request.getOutputModifier());

        Optional<DataElementValue> valueFromAssetConnection = assetConnectionManager.readValue(reference);
        if (valueFromAssetConnection.isPresent()) {
            ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
            if (!Objects.equals(valueFromAssetConnection, oldValue)) {
                submodelElement = ElementValueMapper.setValue(submodelElement, valueFromAssetConnection.get());
                persistence.put(null, reference, submodelElement);
                messageBus.publish(ValueChangeEventMessage.builder()
                        .element(reference)
                        .oldValue(oldValue)
                        .newValue(valueFromAssetConnection.get())
                        .build());
            }
        }
        response.setPayload(submodelElement);
        response.setStatusCode(StatusCode.SUCCESS);
        messageBus.publish(ElementReadEventMessage.builder()
                .element(reference)
                .value(submodelElement)
                .build());
        return response;
    }
}
