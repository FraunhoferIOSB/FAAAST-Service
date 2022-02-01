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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathUtils;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingUtils;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdUtils;


/**
 * class to map HTTP-POST-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}/invoke
 */
public class InvokeOperationRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.POST;
    private static final String PATTERN = "^submodels/(.*?)/submodel/submodel-elements/(.*)/invoke$";
    private static final String QUERY_PARAM_ASYNC = "async";
    private static final String QUERY_PARAM_CONTENT = "content";

    @Override
    public Request parse(HttpRequest httpRequest) {
        if (httpRequest.getPathElements() == null || httpRequest.getPathElements().size() != 6) {
            throw new IllegalArgumentException(String.format("invalid URL format (request: %s, url pattern: %s)",
                    InvokeOperationRequest.class.getSimpleName(),
                    PATTERN));
        }
        InvokeOperationRequest request;
        if (Boolean.parseBoolean(httpRequest.getQueryParameter(QUERY_PARAM_ASYNC))) {
            request = parseBody(httpRequest, InvokeOperationAsyncRequest.class);
        }
        else {
            request = parseBody(httpRequest, InvokeOperationSyncRequest.class);
        }
        if (httpRequest.hasQueryParameter(QUERY_PARAM_CONTENT)) {
            request.setContent(Content.fromString(httpRequest.getQueryParameter(QUERY_PARAM_CONTENT)));
        }
        request.setId(IdUtils.parseIdentifier(EncodingUtils.base64Decode(httpRequest.getPathElements().get(1))));
        request.setPath(ElementPathUtils.toKeys(EncodingUtils.urlDecode(httpRequest.getPathElements().get(4))));
        return request;
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return httpRequest.getMethod().equals(HTTP_METHOD)
                && httpRequest.getPath().matches(PATTERN);
    }
}
