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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.AasRequestContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import java.util.Map;


/**
 * class to map HTTP-POST-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 */
public class InvokeOperationRequestMapper extends RequestMapper {

    private static final String SUBMODEL_ID = "submodelId";
    private static final String SUBMODEL_ELEMENT_PATH = "submodelElementPath";
    private static final String PATTERN = String.format(
            "submodels/(?<%s>.*?)/submodel/submodel-elements/(?<%s>.*)/invoke",
            SUBMODEL_ID,
            SUBMODEL_ELEMENT_PATH);
    private static final String QUERY_PARAMETER_ASYNC = "async";

    public InvokeOperationRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN, new AasRequestContext());
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        boolean async = httpRequest.hasQueryParameter(QUERY_PARAMETER_ASYNC)
                && Boolean.parseBoolean(httpRequest.getQueryParameter(QUERY_PARAMETER_ASYNC));
        InvokeOperationRequest request = async
                ? parseBody(httpRequest, InvokeOperationAsyncRequest.class)
                : parseBody(httpRequest, InvokeOperationSyncRequest.class);
        request.setId(IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(urlParameters.get(SUBMODEL_ID))));
        request.setPath(ElementPathHelper.toKeys(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH))));
        return request;
    }
}
