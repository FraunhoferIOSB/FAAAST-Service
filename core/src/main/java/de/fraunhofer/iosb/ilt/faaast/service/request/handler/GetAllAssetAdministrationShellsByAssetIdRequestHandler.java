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
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByAssetIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByAssetIdResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellsByAssetIdRequestHandler
        extends RequestHandler<GetAllAssetAdministrationShellsByAssetIdRequest, GetAllAssetAdministrationShellsByAssetIdResponse> {

    public GetAllAssetAdministrationShellsByAssetIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllAssetAdministrationShellsByAssetIdResponse process(GetAllAssetAdministrationShellsByAssetIdRequest request) throws MessageBusException {
        GetAllAssetAdministrationShellsByAssetIdResponse response = new GetAllAssetAdministrationShellsByAssetIdResponse();
        List<AssetIdentification> assetIdentifications = new ArrayList<>();
        List<IdentifierKeyValuePair> identifierKeyValuePairs = request.getAssetIds();
        for (IdentifierKeyValuePair pair: identifierKeyValuePairs) {
            AssetIdentification id = null;
            if (pair.getKey().equalsIgnoreCase("globalAssetId")) {
                id = new GlobalAssetIdentification.Builder()
                        .reference(new DefaultReference.Builder().key(new DefaultKey.Builder()
                                .idType(KeyType.IRI)
                                .type(KeyElements.GLOBAL_REFERENCE)
                                .value(pair.getValue())
                                .build())
                                .build())
                        .build();
            }
            else {
                id = new SpecificAssetIdentification.Builder()
                        .value(pair.getValue())
                        .key(pair.getKey())
                        .build();
            }
            assetIdentifications.add(id);
        }

        List<AssetAdministrationShell> shells = new ArrayList<>(persistence.get(null, assetIdentifications, request.getOutputModifier()));
        response.setPayload(shells);
        response.setStatusCode(StatusCode.SUCCESS);
        shells.forEach(LambdaExceptionHelper.rethrowConsumer(
                x -> messageBus.publish(ElementReadEventMessage.builder()
                        .element(x)
                        .value(x)
                        .build())));
        return response;
    }

}
