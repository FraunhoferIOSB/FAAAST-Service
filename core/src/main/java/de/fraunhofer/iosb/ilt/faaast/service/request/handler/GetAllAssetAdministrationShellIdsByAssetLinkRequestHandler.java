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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;


public class GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler
        extends RequestHandler<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    public GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkResponse process(GetAllAssetAdministrationShellIdsByAssetLinkRequest request) {
        GetAllAssetAdministrationShellIdsByAssetLinkResponse response = new GetAllAssetAdministrationShellIdsByAssetLinkResponse();
        try {
            //TODO: implement
            //TODO: How to distinguish between GlobalAssetId and SpecificAssetId?

            response.setStatusCode(StatusCode.ServerInternalError);
        }
        catch (Exception ex) {
            response.setStatusCode(StatusCode.ServerInternalError);
        }
        return response;
    }
}
