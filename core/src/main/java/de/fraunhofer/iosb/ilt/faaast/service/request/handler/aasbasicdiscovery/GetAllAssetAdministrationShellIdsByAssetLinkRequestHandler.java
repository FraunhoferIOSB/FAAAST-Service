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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.SearchAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.SearchAllAssetAdministrationShellIdsByAssetLinkResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;


/**
 * Class to handle a {@link GetAllAssetAdministrationShellIdsByAssetLinkRequest} in the service and to send the
 * corresponding response
 * {@link GetAllAssetAdministrationShellIdsByAssetLinkResponse}. Is responsible for communication with the persistence
 * and sends the corresponding events to the message bus.
 */
@Deprecated(since = "1.4")
public class GetAllAssetAdministrationShellIdsByAssetLinkRequestHandler
        extends AbstractRequestHandler<GetAllAssetAdministrationShellIdsByAssetLinkRequest, GetAllAssetAdministrationShellIdsByAssetLinkResponse> {

    @Override
    public GetAllAssetAdministrationShellIdsByAssetLinkResponse process(GetAllAssetAdministrationShellIdsByAssetLinkRequest request, RequestExecutionContext context)
            throws PersistenceException {
        // Since this request is deprecated and has the same functionality as SearchAllAssetAdministrationShellIdsByAssetLink, use its handler
        // If the functionality of SearchAllAssetAdministrationShellIdsByAssetLink changes, roll back
        SearchAllAssetAdministrationShellIdsByAssetLinkRequest searchAllAssetAdministrationShellIdsByAssetLinkRequest = request
                .asSearchAllAssetAdministrationShellIdsByAssetLinkRequest();

        SearchAllAssetAdministrationShellIdsByAssetLinkResponse searchAllAssetAdministrationShellIdsByAssetLinkResponse = new SearchAllAssetAdministrationShellIdsByAssetLinkRequestHandler()
                .process(
                        searchAllAssetAdministrationShellIdsByAssetLinkRequest, context);

        return GetAllAssetAdministrationShellIdsByAssetLinkResponse.from(searchAllAssetAdministrationShellIdsByAssetLinkResponse);
    }
}
