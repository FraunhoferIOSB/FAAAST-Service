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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.aasrepository;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest}
 * in the service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByAssetIdResponse}.
 * Is responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllAssetAdministrationShellsByAssetIdRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellsByAssetIdRequest, GetAllAssetAdministrationShellsByAssetIdResponse> {

    public GetAllAssetAdministrationShellsByAssetIdRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public GetAllAssetAdministrationShellsByAssetIdResponse process(GetAllAssetAdministrationShellsByAssetIdRequest request) throws MessageBusException {
        List<AssetIdentification> assetIdentifications = new ArrayList<>();
        List<SpecificAssetID> identifierKeyValuePairs = request.getAssetIds();
        for (SpecificAssetID pair: identifierKeyValuePairs) {
            AssetIdentification id = null;
            if (pair.getName().equalsIgnoreCase("globalAssetId")) {
                id = new GlobalAssetIdentification.Builder()
                        .value(pair.getValue())
                        .build();
            }
            else {
                id = new SpecificAssetIdentification.Builder()
                        .value(pair.getValue())
                        .key(pair.getName())
                        .build();
            }
            assetIdentifications.add(id);
        }
        List<AssetAdministrationShell> shells = new ArrayList<>(context.getPersistence().findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .assetIds(assetIdentifications)
                        .build(),
                request.getOutputModifier(),
                PagingInfo.ALL));
        shells.forEach(LambdaExceptionHelper.rethrowConsumer(
                x -> context.getMessageBus().publish(ElementReadEventMessage.builder()
                        .element(x)
                        .value(x)
                        .build())));
        return GetAllAssetAdministrationShellsByAssetIdResponse.builder()
                .payload(shells)
                .success()
                .build();
    }

}
