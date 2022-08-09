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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.Map;


/**
 * class to map HTTP-PUT-Request paths:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 * <br>
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 */
public class PutSubmodelElementByPathRequestMapper extends AbstractSubmodelInterfaceRequestMapper<PutSubmodelElementByPathRequest, PutSubmodelElementByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s", pathElement(SUBMODEL_ELEMENT_PATH));

    public PutSubmodelElementByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && !httpRequest.hasQueryParameter(QueryParameters.CONTENT);
    }


    @Override
    public PutSubmodelElementByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        return PutSubmodelElementByPathRequest.builder()
                .path(ElementPathHelper.toKeys(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH))))
                .submodelElement(parseBody(httpRequest, SubmodelElement.class))
                .build();
    }
}
