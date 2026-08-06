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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByProductIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPByProductIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;

import java.util.List;


/**
 * Class to handle a {@link ReadDppByProductIdRequest} in the service and to send the corresponding {@link ReadDPPByProductIdResponse}. Is responsible for communication with the
 * persistence and sends the corresponding events to the message bus.
 */
public class ReadDppByProductIdRequestHandler extends AbstractDppRequestHandler<ReadDppByProductIdRequest, ReadDPPByProductIdResponse> {
    @Override
    public ReadDPPByProductIdResponse process(ReadDppByProductIdRequest request, RequestExecutionContext context) throws Exception {
        AssetIdentification globalAssetId = GlobalAssetIdentification.builder().value(request.getId()).build();
        List<AssetAdministrationShell> aas = context.getPersistence()
                .findAssetAdministrationShells(AssetAdministrationShellSearchCriteria.builder().assetId(globalAssetId).build(), QueryModifier.MAXIMAL,
                        PagingInfo.ALL)
                .getContent();

        if (aas.isEmpty()) {
            throw new ResourceNotFoundException(String.format("No DPP found for product ID %s", request.getId()));
        }
        else if (aas.size() > 1) {
            throw new IllegalStateException(String.format("Multiple DPPs found for product ID %s", request.getId()));
        }

        DigitalProductPassport dpp = buildFrom(aas.get(0), context.getPersistence());

        if (!request.isInternal()) {
            distributeReadEvents(dpp, context.getMessageBus());
        }

        return ReadDPPByProductIdResponse.builder().payload(dpp).success().build();
    }
}
