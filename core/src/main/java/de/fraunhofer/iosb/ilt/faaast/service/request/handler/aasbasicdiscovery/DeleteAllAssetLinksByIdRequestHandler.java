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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aasbasicdiscovery;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.DeleteAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.DeleteAllAssetLinksByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.DeleteAllAssetLinksByIdRequest} in
 * the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.DeleteAllAssetLinksByIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class DeleteAllAssetLinksByIdRequestHandler extends AbstractRequestHandler<DeleteAllAssetLinksByIdRequest, DeleteAllAssetLinksByIdResponse> {

    public DeleteAllAssetLinksByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public DeleteAllAssetLinksByIdResponse process(DeleteAllAssetLinksByIdRequest request) throws ResourceNotFoundException {
        DeleteAllAssetLinksByIdResponse response = new DeleteAllAssetLinksByIdResponse();
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        aas.getAssetInformation().setGlobalAssetId(null);
        aas.getAssetInformation().getSpecificAssetIds().clear();
        context.getPersistence().save(aas);
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        return response;
    }

}
