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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapperWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppIdsByProductIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.ReadDPPIdsByProductIdsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;

import java.util.Map;


/**
 * class to map HTTP-POST-Request path: v1/dppsByProductIds.
 */
public class ReadDppIdsByProductIdsRequestMapper extends AbstractRequestMapperWithPaging<ReadDppIdsByProductIdsRequest, ReadDPPIdsByProductIdsResponse> {

    private static final String PATTERN = "v1/dppsByProductIds";

    public ReadDppIdsByProductIdsRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.POST, PATTERN);
    }


    @Override
    public ReadDppIdsByProductIdsRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, PagingInfo pagingInfo) throws InvalidRequestException {
        try {
            return ReadDppIdsByProductIdsRequest.builder()
                    .productIds(deserializer.readList(httpRequest.getBodyAsString(), String.class))
                    .build();
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format("error deserializing request body (value: %s)", httpRequest.getBodyAsString()), e);
        }

    }
}
