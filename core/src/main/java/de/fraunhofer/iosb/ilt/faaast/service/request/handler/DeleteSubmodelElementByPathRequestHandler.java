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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelElementByPathRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelElementByPathResponse}. Is responsible
 * for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<DeleteSubmodelElementByPathRequest, DeleteSubmodelElementByPathResponse> {

    public DeleteSubmodelElementByPathRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    protected DeleteSubmodelElementByPathResponse doProcess(DeleteSubmodelElementByPathRequest request) throws Exception {
        DeleteSubmodelElementByPathResponse response = new DeleteSubmodelElementByPathResponse();
        Reference reference = ReferenceHelper.toReference(request.getPath(), request.getSubmodelId(), Submodel.class);
        SubmodelElement submodelElement = persistence.get(reference, QueryModifier.DEFAULT);
        persistence.remove(reference);
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        messageBus.publish(ElementDeleteEventMessage.builder()
                .element(reference)
                .value(submodelElement)
                .build());
        return response;
    }
}
