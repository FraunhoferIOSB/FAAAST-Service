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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PostSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PostSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PostSubmodelReferenceRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PostSubmodelReferenceResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelReferenceRequestHandler extends AbstractRequestHandler<PostSubmodelReferenceRequest, PostSubmodelReferenceResponse> {

    public PostSubmodelReferenceRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PostSubmodelReferenceResponse process(PostSubmodelReferenceRequest request) throws ResourceNotFoundException, MessageBusException, ResourceAlreadyExistsException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        if (aas.getSubmodels().contains(request.getSubmodelRef())) {
            throw new ResourceAlreadyExistsException(request.getSubmodelRef());
        }
        aas.getSubmodels().add(request.getSubmodelRef());
        context.getPersistence().save(aas);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(aas)
                    .value(aas)
                    .build());
        }
        return PostSubmodelReferenceResponse.builder()
                .payload(request.getSubmodelRef())
                .created()
                .build();
    }

}
