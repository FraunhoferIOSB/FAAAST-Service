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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDPPIdsByProductIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPIdsByProductIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;


/**
 * Class to handle a {@link ReadDPPIdsByProductIdsRequest} in the service and to send the corresponding
 * {@link ReadDPPIdsByProductIdsResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class ReadDPPIdsByProductIdsRequestHandler extends AbstractRequestHandler<ReadDPPIdsByProductIdsRequest, ReadDPPIdsByProductIdsResponse> {
    @Override
    public ReadDPPIdsByProductIdsResponse process(ReadDPPIdsByProductIdsRequest request, RequestExecutionContext context) throws Exception {
        AssetAdministrationShellSearchCriteria.Builder criteriaBuilder = AssetAdministrationShellSearchCriteria.builder();

        request.getProductIds().stream().map(id -> GlobalAssetIdentification.builder().value(id).build()).forEach(criteriaBuilder::assetId);

        Page<AssetAdministrationShell> dpps = context.getPersistence().findAssetAdministrationShells(criteriaBuilder.build(), QueryModifier.MAXIMAL, PagingInfo.ALL);

        List<String> result = dpps.getContent().stream()
                .map(Identifiable::getId)
                .toList();

        return ReadDPPIdsByProductIdsResponse.builder()
                .payload(Page.<String> builder()
                        .metadata(dpps.getMetadata())
                        .result(result)
                        .build())
                .success()
                .build();
    }
}
