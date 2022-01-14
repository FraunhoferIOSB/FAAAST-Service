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
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdUtils;


/**
 * class to map HTTP-PUT-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 */
public class SetSubmodelElementValueByPathRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.PUT;
    private static final String PATTERN = "^submodels/(.*?)/submodel/submodel-elements/(.*)$";
    private static final String QUERYPARAM1 = "content";
    private static final String QUERYVALUE1 = "value";

    @Override
    public Request parse(HttpRequest httpRequest) {
        if (httpRequest.getPathElements() == null || httpRequest.getPathElements().size() != 5) {
            throw new IllegalArgumentException(String.format("invalid URL format (request: %s, url pattern: %s)",
                    SetSubmodelElementValueByPathRequest.class.getSimpleName(),
                    PATTERN));
        }
        SetSubmodelElementValueByPathRequest request = new SetSubmodelElementValueByPathRequest();
        request.setId(IdUtils.parseIdentifier(EncodingUtils.base64Decode(httpRequest.getPathElements().get(1))));
        request.setPath(ElementPathUtils.toKeys(EncodingUtils.urlDecode(httpRequest.getPathElements().get(4))));
        // approach: pass formatter down to execution of request

        // TODO converting/parsing string to ElementValue not implemented yet but required here
        // value parsing depends on type of property but this should not be resolved this early?!!?
        // see https://app.swaggerhub.com/domains/Plattform_i40/Shared-Domain-Models/Final-Draft#/components/schemas/SubmodelElementValue
        // theoretically could be determined via structure by Jackson which class to use
        // to be on safe side/deterministic, we would need to resolve idShortPath and lookup datatype
        // alternative, forward unparsed data (as JsonNode) and parse later
        //request.setPayload(IdGenerator.parseValue(httpRequest.getBody()));
        return request;
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return httpRequest.getMethod().equals(HTTP_METHOD)
                && httpRequest.getPath().matches(PATTERN)
                // TODO this is obviously wrong as it does not check of content=value but only appearance of key 'content' and value 'value'
                && httpRequest.getQueryParameters().containsKey(QUERYPARAM1)
                && httpRequest.getQueryParameters().containsValue(QUERYVALUE1);
    }
}
