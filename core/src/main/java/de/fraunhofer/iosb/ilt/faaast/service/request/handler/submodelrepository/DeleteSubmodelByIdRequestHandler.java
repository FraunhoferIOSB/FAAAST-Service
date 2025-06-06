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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodelrepository;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.DeleteSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.DeleteSubmodelByIdRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.DeleteSubmodelByIdResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteSubmodelByIdRequestHandler extends AbstractRequestHandler<DeleteSubmodelByIdRequest, DeleteSubmodelByIdResponse> {

    @Override
    public DeleteSubmodelByIdResponse process(DeleteSubmodelByIdRequest request, RequestExecutionContext context)
            throws ResourceNotFoundException, MessageBusException, AssetConnectionException, PersistenceException {
        DeleteSubmodelByIdResponse response = new DeleteSubmodelByIdResponse();
        Submodel submodel = context.getPersistence().getSubmodel(request.getSubmodelId(), QueryModifier.DEFAULT);
        context.getPersistence().deleteSubmodel(request.getSubmodelId());
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        cleanupDanglingAssetConnectionsForParent(ReferenceBuilder.forSubmodel(submodel), context.getPersistence(), context);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementDeleteEventMessage.builder()
                    .element(submodel)
                    .value(submodel)
                    .build());
        }
        return response;
    }
}
