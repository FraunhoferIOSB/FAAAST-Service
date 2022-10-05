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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.AbstractMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper.AbstractResponseMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Comparator;
import org.apache.commons.lang3.tuple.Pair;


/**
 * Maps a given API response to HTTP by finding the best suited (most specific) response mapper.
 */
public class ResponseMappingManager extends AbstractMappingManager<AbstractResponseMapper> {

    public ResponseMappingManager(ServiceContext serviceContext) {
        super(AbstractResponseMapper.class, serviceContext);
    }


    /**
     * Maps a given API response to HTTP by finding the best suited (most specific) response mapper.
     *
     * @param apiRequest the original API request received
     * @param apiResponse the API response to process
     * @param httpResponse the HTTP response to write to
     * @throws IllegalArgumentException is apiRequest is null
     *             * @throws IllegalArgumentException is apiResponse is null
     *             * @throws IllegalArgumentException is httpResponse is null
     */
    public void map(Request apiRequest, Response apiResponse, HttpServletResponse httpResponse) {
        Ensure.requireNonNull(apiRequest, "apiRequest must be non-null");
        Ensure.requireNonNull(apiResponse, "apiResponse must be non-null");
        Ensure.requireNonNull(httpResponse, "httpResponse must be non-null");
        mappers.stream()
                .map(x -> Pair.of(x, TypeToken.of(x.getClass()).resolveType(AbstractResponseMapper.class.getTypeParameters()[0]).getRawType()))
                .filter(x -> x.getValue().isAssignableFrom(apiResponse.getClass()))
                .sorted(Comparator.comparing(Pair::getValue, new MostSpecificClassComparator()))
                .map(Pair::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("no matching response mapper found for type '%s'", apiResponse.getClass())))
                .map(apiRequest, apiResponse, httpResponse);
    }

}
