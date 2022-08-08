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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAASXPackageIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * class to map HTTP-GET-Request path: packages?aasId={aasIds}
 */
public class GetAllAASXPackageIdsRequestMapper extends RequestMapper {

    private static final String PATTERN = "packages";

    public GetAllAASXPackageIdsRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return super.matches(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.AAS_ID);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) {
        return GetAllAASXPackageIdsRequest.builder()
                .aasIds(Stream.of(EncodingHelper.base64Decode(httpRequest.getQueryParameter(QueryParameters.AAS_ID)).split(","))
                        .map(x -> IdentifierHelper.parseIdentifier(x))
                        .collect(Collectors.toList()))
                .build();
    }
}
