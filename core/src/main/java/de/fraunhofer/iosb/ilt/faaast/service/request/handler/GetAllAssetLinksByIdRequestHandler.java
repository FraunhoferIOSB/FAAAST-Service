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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetLinksByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetLinksByIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetLinksByIdResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GetAllAssetLinksByIdRequestHandler extends RequestHandler<GetAllAssetLinksByIdRequest, GetAllAssetLinksByIdResponse> {

    public GetAllAssetLinksByIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllAssetLinksByIdResponse process(GetAllAssetLinksByIdRequest request) throws ResourceNotFoundException {
        GetAllAssetLinksByIdResponse response = new GetAllAssetLinksByIdResponse();
        AssetAdministrationShell aas = (AssetAdministrationShell) persistence.get(request.getId(), QueryModifier.DEFAULT);
        List<IdentifierKeyValuePair> result = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        if (aas.getAssetInformation().getGlobalAssetId() != null
                && aas.getAssetInformation().getGlobalAssetId().getKeys() != null
                && !aas.getAssetInformation().getGlobalAssetId().getKeys().isEmpty()) {
            result.add(new DefaultIdentifierKeyValuePair.Builder()
                    .key(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(aas.getAssetInformation().getGlobalAssetId().getKeys().get(aas.getAssetInformation().getGlobalAssetId().getKeys().size() - 1).getValue())
                    .build());
        }
        response.setPayload(result);
        response.setStatusCode(StatusCode.SUCCESS);
        return response;
    }

}
