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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class SetSubmodelElementValueByPathRequestHandler extends RequestHandler<SetSubmodelElementValueByPathRequest<?>, SetSubmodelElementValueByPathResponse> {

    public SetSubmodelElementValueByPathRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public SetSubmodelElementValueByPathResponse process(SetSubmodelElementValueByPathRequest request) throws ResourceNotFoundException, Exception {
        if (request == null || request.getValueParser() == null) {
            throw new IllegalArgumentException("value parser of request must be non-null");
        }
        SetSubmodelElementValueByPathResponse response = new SetSubmodelElementValueByPathResponse();
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getId(), Submodel.class);
        SubmodelElement submodelElement = persistence.get(reference, new OutputModifier.Builder()
                .extend(Extent.WITH_BLOB_VALUE)
                .build());
        ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
        ElementValue newValue = request.getValueParser().parse(request.getRawValue(), oldValue.getClass());
        ElementValueMapper.setValue(submodelElement, newValue);
        assetConnectionManager.setValue(reference, newValue);
        try {
            persistence.put(null, reference, submodelElement);
        }
        catch (IllegalArgumentException e) {
            // empty on purpose
        }
        response.setStatusCode(StatusCode.SUCCESS);
        messageBus.publish(ValueChangeEventMessage.builder()
                .element(reference)
                .oldValue(oldValue)
                .newValue(newValue)
                .build());
        return response;
    }

}
