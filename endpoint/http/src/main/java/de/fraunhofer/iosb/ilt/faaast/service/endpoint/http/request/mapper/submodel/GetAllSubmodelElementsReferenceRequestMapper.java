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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapperWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsReferenceResponse;
import java.util.Map;


/**
 * class to map HTTP-GET-Request paths: submodels/{submodelIdentifier}/submodel/submodel-elements/$reference,
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/$reference.
 */
public class GetAllSubmodelElementsReferenceRequestMapper
        extends AbstractSubmodelInterfaceRequestMapperWithPaging<GetAllSubmodelElementsReferenceRequest, GetAllSubmodelElementsReferenceResponse> {

    private static final String PATTERN = "submodel-elements/\\$reference";

    public GetAllSubmodelElementsReferenceRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN, Content.METADATA, Content.NORMAL, Content.PATH, Content.VALUE);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && !httpRequest.hasQueryParameter(QueryParameters.PARENT_PATH);
    }


    @Override
    public GetAllSubmodelElementsReferenceRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier, PagingInfo pagingInfo) {
        return GetAllSubmodelElementsReferenceRequest.builder()
                .build();
    }

}
