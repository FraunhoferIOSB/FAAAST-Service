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
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import java.util.Map;


/**
 * class to map HTTP-DELETE-Request path: submodels/{submodelIdentifier}
 */
public class DeleteSubmodelByIdRequestMapper extends RequestMapper {

    private static final String SUBMODEL_ID = "submodelId";
    private static final String PATTERN = String.format("(?!.*/submodel)submodels/(?<%s>.*)", SUBMODEL_ID);

    public DeleteSubmodelByIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) {
        return DeleteSubmodelByIdRequest.builder()
                .id(IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(urlParameters.get(SUBMODEL_ID))))
                .build();
    }
}
