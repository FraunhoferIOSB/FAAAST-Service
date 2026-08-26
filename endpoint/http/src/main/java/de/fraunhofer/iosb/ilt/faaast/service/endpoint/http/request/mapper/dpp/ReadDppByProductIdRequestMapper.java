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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByProductIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;

import java.util.Map;


/**
 * class to map HTTP-GET-Request path: v1/dppsByProductId/{productId}.
 */
public class ReadDppByProductIdRequestMapper extends AbstractDppRequestMapperWithSerializationMode {

    private static final String PRODUCT_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("v1/dppsByProductId/%s", pathElement(PRODUCT_ID));

    public ReadDppByProductIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public ReadDppByProductIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        return ReadDppByProductIdRequest.builder()
                .id(getParameterBase64UrlEncoded(urlParameters, PRODUCT_ID))
                .dppSerializationMode(parseSerializationMode(httpRequest.getQueryParameter("representation")))
                .build();
    }
}
