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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.AbstractMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Finds matching request mapper for given HTTP request.
 */
public class RequestMappingManager extends AbstractMappingManager<AbstractRequestMapper> {

    public RequestMappingManager(ServiceContext serviceContext) {
        super(AbstractRequestMapper.class, serviceContext);
    }


    /**
     * Gets a set of supported methods for the url or an empty set if url is not supported at all.
     *
     * @param url the URL to check
     * @return a set of supported methods for the url
     */
    public Set<HttpMethod> getSupportedMethods(String url) {
        return mappers.stream()
                .filter(request -> request.matchesUrl(url))
                .map(x -> x.getMethod())
                .distinct()
                .collect(Collectors.toSet());
    }


    /**
     * Finds corresponding protocol-agnostic request for given HTTP request.
     *
     * @param httpRequest HTTP-based request to find a suitable request mapper
     * @return protocol-agnostic request
     * @throws InvalidRequestException if no mapper is found for request
     * @throws MethodNotAllowedException if the method was not valid for the request
     * @throws IllegalStateException if there were multiple matching mappers
     */
    public AbstractRequestMapper findRequestMapper(HttpRequest httpRequest) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        Set<AbstractRequestMapper> mappersByUrl = mappers.stream()
                .filter(request -> request.matchesUrl(httpRequest))
                .collect(Collectors.toSet());
        if (mappersByUrl.isEmpty()) {
            throw new InvalidRequestException(String.format("no matching request mapper found for URL '%s'", httpRequest.getPath()));
        }
        Set<AbstractRequestMapper> mappersByUrlAndMethod = mappersByUrl.stream()
                .filter(x -> x.getMethod() == httpRequest.getMethod())
                .collect(Collectors.toSet());
        if (mappersByUrlAndMethod.isEmpty()) {
            throw new MethodNotAllowedException(httpRequest, mappersByUrl);
        }
        if (mappersByUrlAndMethod.size() > 1) {
            throw new IllegalStateException(String.format(
                    "found multiple request mapper matching HTTP method and URL (HTTP method: %s, url: %s)",
                    httpRequest.getMethod(),
                    httpRequest.getPath()));
        }
        return mappersByUrlAndMethod.iterator().next();
    }


    /**
     * Finds corresponding protocol-agnostic request for given HTTP request and converts it.
     *
     * @param httpRequest HTTP-based request to convert
     * @return protocol-agnostic request
     * @throws InvalidRequestException if no mapper is found for request or mapping fails
     * @throws MethodNotAllowedException if HTTP method is not allowed on URL
     */
    public Request map(HttpRequest httpRequest) throws InvalidRequestException {
        return findRequestMapper(httpRequest).parse(httpRequest);
    }

}
