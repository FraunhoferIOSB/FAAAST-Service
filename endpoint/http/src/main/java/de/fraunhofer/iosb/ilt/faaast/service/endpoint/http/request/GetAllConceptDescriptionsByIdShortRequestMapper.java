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
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsByIdShortRequest;


/**
 * class to map HTTP-GET-Request path: concept-descriptions
 */
public class GetAllConceptDescriptionsByIdShortRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.GET;
    private static final String PATTERN = "^concept-descriptions$";
    private static final String QUERYPARAM1 = "idShort";

    @Override
    public Request parse(HttpRequest httpRequest) {
        GetAllConceptDescriptionsByIdShortRequest request = new GetAllConceptDescriptionsByIdShortRequest();
        request.setIdShort(httpRequest.getQueryParameters().get(QUERYPARAM1));
        request.setOutputModifier(new OutputModifier());
        return request;
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return httpRequest.getMethod().equals(HTTP_METHOD)
                && httpRequest.getPath().matches(PATTERN)
                && httpRequest.getQueryParameters().containsKey(QUERYPARAM1);
    }
}
