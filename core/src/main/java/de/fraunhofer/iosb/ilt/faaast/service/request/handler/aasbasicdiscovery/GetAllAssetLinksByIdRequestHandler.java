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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.GetAllAssetLinksByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetID;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetLinksByIdRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.GetAllAssetLinksByIdResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetLinksByIdRequestHandler extends AbstractRequestHandler<GetAllAssetLinksByIdRequest, GetAllAssetLinksByIdResponse> {

    public GetAllAssetLinksByIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllAssetLinksByIdResponse process(GetAllAssetLinksByIdRequest request) throws ResourceNotFoundException {
        AssetAdministrationShell aas = context.getPersistence().getAssetAdministrationShell(request.getId(), QueryModifier.DEFAULT);
        List<SpecificAssetID> result = new ArrayList<>(aas.getAssetInformation().getSpecificAssetIds());
        if (aas.getAssetInformation().getGlobalAssetID() != null) {
            result.add(new DefaultSpecificAssetID.Builder()
                    .name(FaaastConstants.KEY_GLOBAL_ASSET_ID)
                    .value(aas.getAssetInformation().getGlobalAssetID())
                    .build());
        }
        return GetAllAssetLinksByIdResponse.builder()
                .payload(result)
                .success()
                .build();
    }

}
