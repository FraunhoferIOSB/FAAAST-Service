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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import io.adminshell.aas.v3.model.ConceptDescription;


/**
 * class to map HTTP-PUT-Request path: concept-descriptions/{cdIdentifier}
 */
public class PutConceptDescriptionByIdRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.PUT;
    private static final String PATTERN = "^concept-descriptions/(.*)$";

    public PutConceptDescriptionByIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public Request parse(HttpRequest httpRequest) throws InvalidRequestException {
        return PutConceptDescriptionByIdRequest.builder()
                .id(IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(httpRequest.getPathElements().get(1))))
                .conceptDescription(parseBody(httpRequest, ConceptDescription.class))
                .build();
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return httpRequest.getMethod().equals(HTTP_METHOD)
                && httpRequest.getPath().matches(PATTERN);
    }
}
