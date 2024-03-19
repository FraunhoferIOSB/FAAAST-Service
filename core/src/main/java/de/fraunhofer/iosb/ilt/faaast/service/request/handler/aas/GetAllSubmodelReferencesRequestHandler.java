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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aas;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAllSubmodelReferencesRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAllSubmodelReferencesResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAllSubmodelReferencesRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAllSubmodelReferencesResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllSubmodelReferencesRequestHandler extends AbstractRequestHandler<GetAllSubmodelReferencesRequest, GetAllSubmodelReferencesResponse> {

    public GetAllSubmodelReferencesRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllSubmodelReferencesResponse process(GetAllSubmodelReferencesRequest request) throws ResourceNotFoundException, MessageBusException {
        Page<Reference> page = context.getPersistence().getSubmodelRefs(request.getId(), request.getPagingInfo());
        if (!request.isInternal()) {
            AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), OutputModifier.MINIMAL);
            context.getMessageBus().publish(ElementReadEventMessage.builder()
                    .element(aas)
                    .value(aas)
                    .build());
        }
        return GetAllSubmodelReferencesResponse.builder()
                .payload(page)
                .success()
                .build();
    }
}
