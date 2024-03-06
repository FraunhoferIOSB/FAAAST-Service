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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetSubmodelElementByPathRequest, GetSubmodelElementByPathResponse> {

    public GetSubmodelElementByPathRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetSubmodelElementByPathResponse doProcess(GetSubmodelElementByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ResourceNotAContainerElementException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(reference, request.getOutputModifier());
        Optional<DataElementValue> valueFromAssetConnection = context.getAssetConnectionManager().readValue(reference);
        if (valueFromAssetConnection.isPresent()) {
            ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
            if (!Objects.equals(valueFromAssetConnection, oldValue)) {
                submodelElement = ElementValueMapper.setValue(submodelElement, valueFromAssetConnection.get());
                context.getPersistence().update(reference, submodelElement);
                if (!request.isInternal()) {
                    context.getMessageBus().publish(ValueChangeEventMessage.builder()
                            .element(reference)
                            .oldValue(oldValue)
                            .newValue(valueFromAssetConnection.get())
                            .build());
                }
            }
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(reference)
                    .value(submodelElement)
                    .build());
        }
        return GetSubmodelElementByPathResponse.builder()
                .payload(submodelElement)
                .success()
                .build();
    }
}
