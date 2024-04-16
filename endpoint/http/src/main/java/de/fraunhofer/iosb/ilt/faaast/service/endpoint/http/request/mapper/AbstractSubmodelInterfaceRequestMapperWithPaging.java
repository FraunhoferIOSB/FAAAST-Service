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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequestWithPaging;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;


/**
 * Base class for requests that are part of the Submodel Interface API and includes paging information. This class
 * exposes the URL both as the stand-alone URL (e.g. submodels/{submodelIdentifier}/...) as well as the
 * AAS-contextualied version (e.g. /shells/{aasIdentifier}/submodels/{submodelIdentifier}/...).
 *
 * @param <T> type of request
 * @param <R> type of response to the request
 */
public abstract class AbstractSubmodelInterfaceRequestMapperWithPaging<T extends AbstractSubmodelInterfaceRequestWithPaging<R>, R extends Response>
        extends AbstractSubmodelInterfaceRequestMapper<T, R> {

    protected AbstractSubmodelInterfaceRequestMapperWithPaging(ServiceContext serviceContext, HttpMethod method, String urlPattern, Content... excludedContentModifiers) {
        super(serviceContext, method, urlPattern, excludedContentModifiers);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output modifier information.
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @param outputModifier output modifier for this request
     * @param pagingInfo the paging information
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier, PagingInfo pagingInfo) throws InvalidRequestException;


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        try {
            PagingInfo pagingInfo = PagingHelper.parsePagingInfo(httpRequest.getQueryParameters());
            T result = doParse(httpRequest, urlParameters, outputModifier, pagingInfo);
            result.setPagingInfo(pagingInfo);
            return result;
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException("invalid output modifier", e);
        }
    }

}
