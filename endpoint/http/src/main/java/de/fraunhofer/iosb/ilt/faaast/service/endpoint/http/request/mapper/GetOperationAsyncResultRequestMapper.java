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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.AasRequestContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.RequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.Map;


/**
 * class to map HTTP-GET-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/operation-Results/(.*)
 */
public class GetOperationAsyncResultRequestMapper extends RequestMapperWithOutputModifier<GetOperationAsyncResultRequest, GetOperationAsyncResultResponse> {

    private static final String SUBMODEL_ID = "submodelId";
    private static final String SUBMODEL_ELEMENT_PATH = "submodelElementPath";
    private static final String HANDLE_ID = "handleId";
    private static final String PATTERN = String.format(
            "submodels/(?<%s>.*?)/submodel/submodel-elements/(?<%s>.*)/operation-results/(?<%s>.*)",
            SUBMODEL_ID,
            SUBMODEL_ELEMENT_PATH,
            HANDLE_ID);

    public GetOperationAsyncResultRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN, new AasRequestContext());
    }


    @Override
    public RequestWithModifier doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) {
        return GetOperationAsyncResultRequest.builder()
                .path(ElementPathHelper.toKeys(urlParameters.get(SUBMODEL_ELEMENT_PATH)))
                .handleId(EncodingHelper.base64Decode(urlParameters.get(HANDLE_ID)))
                .outputModifier(outputModifier)
                .build();
    }
}
