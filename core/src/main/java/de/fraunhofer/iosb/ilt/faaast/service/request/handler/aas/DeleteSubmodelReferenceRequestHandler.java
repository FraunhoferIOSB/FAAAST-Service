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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.DeleteSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteSubmodelReferenceRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.DeleteSubmodelReferenceResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteSubmodelReferenceRequestHandler extends AbstractRequestHandler<DeleteSubmodelReferenceRequest, DeleteSubmodelReferenceResponse> {

    public DeleteSubmodelReferenceRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public DeleteSubmodelReferenceResponse process(DeleteSubmodelReferenceRequest request) throws ResourceNotFoundException, MessageBusException {
        DeleteSubmodelReferenceResponse response = new DeleteSubmodelReferenceResponse();
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        Reference submodelRefToDelete = aas.getSubmodels().stream()
                .filter(x -> ReferenceHelper.equals(request.getSubmodelRef(), x))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                        "SubmodelReference '%s' not found in AAS with id '%s'",
                        ReferenceHelper.toString(request.getSubmodelRef()),
                        request.getId())));
        aas.getSubmodels().remove(submodelRefToDelete);
        context.getPersistence().save(aas);
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(aas)
                    .value(aas)
                    .build());
        }
        return response;
    }

}
