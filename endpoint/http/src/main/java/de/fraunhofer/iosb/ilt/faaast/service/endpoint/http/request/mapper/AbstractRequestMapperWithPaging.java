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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.PagingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;


/**
 * Base class for mapping HTTP requests including paging information.
 *
 * @param <T> type of request
 * @param <R> type of response to the request
 */
public abstract class AbstractRequestMapperWithPaging<T extends AbstractRequestWithPaging<R>, R extends Response> extends AbstractRequestMapper {

    protected AbstractRequestMapperWithPaging(ServiceContext serviceContext, HttpMethod method, String urlPattern) {
        super(serviceContext, method, urlPattern);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output modifier information.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @param pagingInfo the paging information
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, PagingInfo pagingInfo) throws InvalidRequestException;


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        try {
            PagingInfo pagingInfo = PagingHelper.parsePagingInfo(httpRequest.getQueryParameters());
            T result = doParse(httpRequest, urlParameters, pagingInfo);
            result.setPagingInfo(pagingInfo);
            return result;
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException("invalid output modifier", e);
        }
    }

}
