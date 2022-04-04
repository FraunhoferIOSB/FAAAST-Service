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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAllAssetLinksByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAllAssetLinksByIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAllAssetLinksByIdResponse}.
 * Is responsible for communication with the persistence and sends the
 * corresponding events to the message bus.
 */
public class PostAllAssetLinksByIdRequestHandler extends RequestHandler<PostAllAssetLinksByIdRequest, PostAllAssetLinksByIdResponse> {

    public PostAllAssetLinksByIdRequestHandler(Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PostAllAssetLinksByIdResponse process(PostAllAssetLinksByIdRequest request) throws ResourceNotFoundException {
        PostAllAssetLinksByIdResponse response = new PostAllAssetLinksByIdResponse();
        AssetAdministrationShell aas = (AssetAdministrationShell) persistence.get(request.getId(), QueryModifier.DEFAULT);
        List<IdentifierKeyValuePair> globalKeys = request.getAssetLinks().stream()
                .filter(x -> FaaastConstants.KEY_GLOBAL_ASSET_ID.equals(x.getKey()))
                .collect(Collectors.toList());
        if (!globalKeys.isEmpty()) {
            if (globalKeys.size() == 1 && globalKeys.get(0) != null) {
                Reference parsedReference = AasUtils.parseReference(globalKeys.get(0).getValue());
                aas.getAssetInformation().setGlobalAssetId(parsedReference != null
                        ? parsedReference
                        : new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .idType(KeyType.IRI)
                                        .type(KeyElements.ASSET)
                                        .value(globalKeys.get(0).getValue())
                                        .build())
                                .build());
            }
            else {
                response.setError(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                        String.format("request can contain at most 1 element with key '%s', but %d found",
                                FaaastConstants.KEY_GLOBAL_ASSET_ID,
                                globalKeys.size()));
                return response;
            }
        }
        List<IdentifierKeyValuePair> newSpecificAssetIds = request.getAssetLinks().stream()
                .filter(x -> !Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getKey()))
                .collect(Collectors.toList());
        for (var newSpecificAssetId: newSpecificAssetIds) {
            List<IdentifierKeyValuePair> existingLinks = aas.getAssetInformation().getSpecificAssetIds().stream()
                    .filter(x -> Objects.equals(x.getKey(), newSpecificAssetId.getKey()))
                    .collect(Collectors.toList());
            if (existingLinks.isEmpty()) {
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else if (existingLinks.size() == 1) {
                aas.getAssetInformation().getSpecificAssetIds().remove(existingLinks.get(0));
                aas.getAssetInformation().getSpecificAssetIds().add(newSpecificAssetId);
            }
            else {
                response.setError(StatusCode.CLIENT_ERROR_BAD_REQUEST,
                        String.format("error updating specificAssetId - found %d entries for key '%s', but expected only one",
                                existingLinks.size(),
                                newSpecificAssetId.getKey()));
                return response;
            }
        }
        aas = (AssetAdministrationShell) persistence.put(aas);
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
        response.setStatusCode(StatusCode.SUCCESS_CREATED);
        return response;
    }

}
