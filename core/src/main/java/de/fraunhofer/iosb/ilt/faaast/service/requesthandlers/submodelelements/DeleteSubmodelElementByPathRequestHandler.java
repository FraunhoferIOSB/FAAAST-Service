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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.submodelelements;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.DeleteSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.RequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.Util;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;


public class DeleteSubmodelElementByPathRequestHandler extends RequestHandler<DeleteSubmodelElementByPathRequest, DeleteSubmodelElementByPathResponse> {

    public DeleteSubmodelElementByPathRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public DeleteSubmodelElementByPathResponse process(DeleteSubmodelElementByPathRequest request) {
        DeleteSubmodelElementByPathResponse response = new DeleteSubmodelElementByPathResponse();

        try {
            Reference reference = Util.toReference(request.getPath(), request.getId(), Submodel.class);
            SubmodelElement submodelElement = persistence.get(reference, new QueryModifier());
            persistence.remove(reference);
            response.setStatusCode(StatusCode.Success);
            publishElementDeleteEventMessage(reference, submodelElement);
        }
        catch (ResourceNotFoundException ex) {
            response.setStatusCode(StatusCode.ClientErrorResourceNotFound);
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }
}
