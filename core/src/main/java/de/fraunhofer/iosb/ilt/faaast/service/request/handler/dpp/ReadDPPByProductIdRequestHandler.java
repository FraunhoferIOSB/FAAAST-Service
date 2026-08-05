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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDPPByProductIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPByProductIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;


/**
 * Class to handle a {@link ReadDPPByProductIdRequest} in the service and to send the corresponding
 * {@link ReadDPPByProductIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class ReadDPPByProductIdRequestHandler extends AbstractDPPRequestHandler<ReadDPPByProductIdRequest, ReadDPPByProductIdResponse> {
    @Override
    public ReadDPPByProductIdResponse process(ReadDPPByProductIdRequest request, RequestExecutionContext context) throws Exception {
        AssetIdentification globalAssetId = GlobalAssetIdentification.builder().value(request.getProductId()).build();
        List<AssetAdministrationShell> aas = context.getPersistence()
                .findAssetAdministrationShells(AssetAdministrationShellSearchCriteria.builder().assetId(globalAssetId).build(), QueryModifier.MAXIMAL,
                        PagingInfo.ALL)
                .getContent();

        if (aas.size() != 1) {
            return ReadDPPByProductIdResponse.builder().statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND).build();
        }

        DigitalProductPassport dpp = buildFrom(aas.get(0), context.getPersistence());

        if (!request.isInternal()) {
            distributeReadEvents(dpp, context.getMessageBus());
        }

        return ReadDPPByProductIdResponse.builder().payload(dpp).success().build();
    }
}
