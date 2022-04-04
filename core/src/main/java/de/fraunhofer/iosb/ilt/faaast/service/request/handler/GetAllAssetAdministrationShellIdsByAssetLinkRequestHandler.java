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
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Not supported yet! Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellIdsByAssetLinkRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellIdsByAssetLinkResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler
        extends RequestHandler<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    public GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkResponse process(GetAllAssetAdministrationShellIdsByAssetLinkRequest request) {
        GetAllAssetAdministrationShellIdsByAssetLinkResponse response = new GetAllAssetAdministrationShellIdsByAssetLinkResponse();
        // TODO update Persistence interface to forward query; specification does not say whether to use AND or OR on global/specific assetIds
        List<String> globalAssetIds = request.getAssetIdentifierPairs().stream()
                .filter(x -> Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getKey()))
                .map(IdentifierKeyValuePair::getValue)
                .collect(Collectors.toList());
        List<IdentifierKeyValuePair> specificAssetIds = request.getAssetIdentifierPairs().stream()
                .filter(x -> !Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getKey()))
                .collect(Collectors.toList());
        response.setPayload(persistence.getEnvironment().getAssetAdministrationShells().stream()
                .filter(aas -> {
                    boolean globalMatch = aas.getAssetInformation().getGlobalAssetId() != null
                            && aas.getAssetInformation().getGlobalAssetId().getKeys() != null
                            && !aas.getAssetInformation().getGlobalAssetId().getKeys().isEmpty()
                            && globalAssetIds.contains(
                                    aas.getAssetInformation().getGlobalAssetId().getKeys().get(aas.getAssetInformation().getGlobalAssetId().getKeys().size() - 1).getValue());
                    boolean specificMatch = specificAssetIds.stream().allMatch(x -> aas.getAssetInformation().getSpecificAssetIds().contains(x));
                    if (!globalAssetIds.isEmpty() && specificAssetIds.isEmpty()) {
                        return globalMatch;
                    }
                    if (globalAssetIds.isEmpty() && !specificAssetIds.isEmpty()) {
                        return specificMatch;
                    }
                    if (!globalAssetIds.isEmpty() && !specificAssetIds.isEmpty()) {
                        return globalMatch || specificMatch;
                    }
                    return true;
                })
                .map(Identifiable::getIdentification)
                .collect(Collectors.toList()));
        response.setStatusCode(StatusCode.SUCCESS);
        return response;
    }
}
