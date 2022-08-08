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
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import java.util.Map;


/**
 * class to map HTTP-PUT-Request path: shells/{aasIdentifier}/aas
 */
public class PutAssetAdministrationShellRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/(?<%s>.*)/aas/?", AAS_ID);

    public PutAssetAdministrationShellRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        return PutAssetAdministrationShellRequest.builder()
                .id(IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(urlParameters.get(AAS_ID))))
                .aas(parseBody(httpRequest, AssetAdministrationShell.class))
                .build();
    }
}
