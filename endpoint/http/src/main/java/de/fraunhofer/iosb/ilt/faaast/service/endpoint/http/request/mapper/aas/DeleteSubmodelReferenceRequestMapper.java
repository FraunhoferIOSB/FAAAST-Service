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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.aas;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;


/**
 * class to map HTTP-DELETE-Request path: shells/{aasIdentifier}/submodel-refs/{submodelIdentifier}.
 */
public class DeleteSubmodelReferenceRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String SUBMODEL_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/%s/submodel-refs/%s",
            pathElement(AAS_ID),
            pathElement(SUBMODEL_ID));

    public DeleteSubmodelReferenceRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        return DeleteSubmodelReferenceRequest.builder()
                .id(getParameterBase64UrlEncoded(urlParameters, AAS_ID))
                .submodelRef(ReferenceBuilder.forSubmodel(getParameterBase64UrlEncoded(urlParameters, SUBMODEL_ID)))
                .build();
    }
}
