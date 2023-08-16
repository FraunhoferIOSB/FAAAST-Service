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
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAllAssetLinksByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetID;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAllAssetLinksByIdRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAllAssetLinksByIdResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostAllAssetLinksByIdRequestHandler extends AbstractRequestHandler<PostAllAssetLinksByIdRequest, PostAllAssetLinksByIdResponse> {

    public PostAllAssetLinksByIdRequestHandler(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PostAllAssetLinksByIdResponse process(PostAllAssetLinksByIdRequest request) throws ResourceNotFoundException {
        AssetAdministrationShell aas = persistence.getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        List<SpecificAssetID> globalKeys = request.getAssetLinks().stream()
                .filter(x -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(x.getName()))
                .collect(Collectors.toList());
        if (!globalKeys.isEmpty()) {
            if (globalKeys.size() == 1 && globalKeys.get(0) != null) {
                aas.getAssetInformation().setGlobalAssetID(globalKeys.get(0).getValue());
            }
            else {
                return PostAllAssetLinksByIdResponse.builder()
                        .error(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                                String.format("request can contain at most 1 element with key '%s', but %d found",
                                        FaaastConstants.KEY_GLOBAL_ASSET_ID,
                                        globalKeys.size()))
                        .build();
            }
        }
        List<SpecificAssetID> newSpecificAssetIds = request.getAssetLinks().stream()
                .filter(x -> !Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getName()))
                .collect(Collectors.toList());
        for (var newSpecificAssetId: newSpecificAssetIds) {
            List<SpecificAssetID> existingLinks = aas.getAssetInformation().getSpecificAssetIds().stream()
                    .filter(x -> Objects.equals(x.getName(), newSpecificAssetId.getName()))
                    .collect(Collectors.toList());
            if (existingLinks.isEmpty()) {
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else if (existingLinks.size() == 1) {
                aas.getAssetInformation().getSpecificAssetIds().remove(existingLinks.get(0));
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else {
                return PostAllAssetLinksByIdResponse.builder()
                        .error(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                                String.format("error updating specificAssetId - found %d entries for key '%s', but expected only one",
                                        existingLinks.size(),
                                        newSpecificAssetId.getName()))
                        .build();
            }
        }
        persistence.save(aas);
        List<SpecificAssetID> result = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        if (aas.getAssetInformation().getGlobalAssetID() != null) {
            result.add(new DefaultSpecificAssetID.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(aas.getAssetInformation().getGlobalAssetID())
                    .build());
        }
        return PostAllAssetLinksByIdResponse.builder()
                .payload(result)
                .created()
                .build();
    }

}
