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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.utils.ElementPathUtils;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.utils.EncodingUtils;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdUtils;


/**
 * class to map HTTP-GET-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 */
public class GetSubmodelElementByPathRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.GET;
    private static final String PATTERN = "(?!.*/operation-results)^submodels/(.*?)/submodel/submodel-elements/(.*?)$";
    private static final String QUERYPARAM1 = "level";
    private static final String QUERYPARAM2 = "content";
    private static final String QUERYPARAM3 = "extend";

    @Override
    public Request parse(HttpRequest httpRequest) {
        if (httpRequest.getPathElements() == null || httpRequest.getPathElements().size() != 5) {
            throw new IllegalArgumentException(String.format("invalid URL format (request: %s, url pattern: %s)",
                    GetSubmodelElementByPathRequest.class.getSimpleName(),
                    PATTERN));
        }
        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest();
        OutputModifier outputModifier = new OutputModifier();
        outputModifier.setLevel(Level.fromString(httpRequest.getQueryParameters().get(QUERYPARAM1)));
        outputModifier.setContent(Content.fromString(httpRequest.getQueryParameters().get(QUERYPARAM2)));
        outputModifier.setExtend(Extend.fromString(httpRequest.getQueryParameters().get(QUERYPARAM3)));
        request.setOutputModifier(outputModifier);
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
