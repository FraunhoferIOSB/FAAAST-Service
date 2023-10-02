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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-GET-Request path: shells.
 */
public class GetAssetAdministrationShellByIdRequestMapper
        extends AbstractRequestMapperWithOutputModifier<GetAssetAdministrationShellByIdRequest, GetAssetAdministrationShellByIdResponse> {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("(?!.*/aas)shells/%s", pathElement(AAS_ID));

    public GetAssetAdministrationShellByIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public GetAssetAdministrationShellByIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) {
        return GetAssetAdministrationShellByIdRequest.builder()
                .id(IdentifierHelper.parseIdentifier(EncodingHelper.base64UrlDecode(urlParameters.get(AAS_ID))))
                .build();
    }
}