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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelElementsRequest;
import java.util.Map;


/**
 * class to map HTTP-GET-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements
 * <br>
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements
 */
public class GetAllSubmodelElementsRequestMapper extends AbstractSubmodelInterfaceRequestMapper<GetAllSubmodelElementsRequest, GetAllSubmodelElementsResponse> {

    private static final String PATTERN = "submodel-elements";

    public GetAllSubmodelElementsRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && !httpRequest.hasQueryParameter(QueryParameters.PARENT_PATH);
    }


    @Override
    public GetAllSubmodelElementsRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) {
        return GetAllSubmodelElementsRequest.builder()
                .build();
    }

}
