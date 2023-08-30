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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;


/**
 * Not supported yet! Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    public GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkResponse process(GetAllAssetAdministrationShellIdsByAssetLinkRequest request) {
        // TODO update Persistence interface to forward query; specification does not say whether to use AND or OR on global/specific assetIds
        List<String> globalAssetIds = request.getAssetIdentifierPairs().stream()
                .filter(x -> Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getName()))
                .map(SpecificAssetID::getValue)
                .collect(Collectors.toList());
        List<SpecificAssetID> specificAssetIds = request.getAssetIdentifierPairs().stream()
                .filter(x -> !Objects.equals(FaaastConstants.KEY_GLOBAL_ASSET_ID, x.getName()))
                .collect(Collectors.toList());
        List<String> result = context.getPersistence().findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.NONE,
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .stream()
                .filter(aas -> {
                    boolean globalMatch = aas.getAssetInformation().getGlobalAssetID() != null
                            && globalAssetIds.contains(aas.getAssetInformation().getGlobalAssetID());
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
                .map(Identifiable::getId)
                .collect(Collectors.toList());
        return GetAllAssetAdministrationShellIdsByAssetLinkResponse.builder()
                .payload(result)
                .success()
                .build();
    }
}
