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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds matching request mapper for given HTTP request.
 */
public class RequestMappingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingManager.class);
    private List<AbstractRequestMapper> mappers;
    protected ServiceContext serviceContext;

    public RequestMappingManager(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        init();
    }


    private void init() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            mappers = scanResult
                    .getSubclasses(AbstractRequestMapper.class.getName())
                    .filter(x -> !x.isAbstract() && !x.isInterface())
                    .loadClasses(AbstractRequestMapper.class)
                    .stream()
                    .map(x -> {
                        try {
                            return ConstructorUtils.invokeConstructor(x, serviceContext);
                        }
                        catch (NoSuchMethodException | SecurityException e) {
                            LOGGER.warn("request mapper implementation could not be loaded, "
                                    + "reason: missing constructor (implementation class: {}, required constructor signature: {})",
                                    x.getName(),
                                    ServiceContext.class.getName(),
                                    e);
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            LOGGER.warn("request mapper implementation could not be loaded, "
                                    + "reason: calling constructor failed (implementation class: {}, constructor arguments: {})",
                                    x.getName(),
                                    ServiceContext.class.getName(),
                                    e);
                        }
                        LOGGER.debug("unable to instantiate class {}", x.getName());
                        return null;
                    })
                    .filter(x -> x != null)
                    .collect(Collectors.toList());
            mappers.stream()
                    .filter(x -> x == null)
                    .collect(Collectors.toList());
        }
    }


    /**
     * Finds corresponding protocol-agnostic request for given HTTP request.
     *
     * @param httpRequest HTTP-based request to find a suitable request mapper
     * @return protocol-agnostic request
     * @throws InvalidRequestException if no mapper is found for request
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
            throw new MethodNotAllowedException(String.format(
                    "method '%s' not allowed for URL '%s' (allowed methods: %s)",
                    httpRequest.getMethod(),
                    httpRequest.getPath(),
                    mappersByUrl.stream()
                            .map(x -> x.getMethod().name())
                            .distinct()
                            .sorted()
                            .collect(Collectors.joining(", "))));
        }
        if (mappersByUrlAndMethod.size() > 1) {
            throw new IllegalStateException(String.format(
                    "found multiple request mapper matching HTTP method and URL (HTTP method: %s, url: %s",
                    httpRequest.getMethod(),
                    httpRequest.getPath()));
        }
        return mappersByUrlAndMethod.iterator().next();
    }


    /**
     * Finds corresponding protocol-agnostic request for given HTTP request and
     * converts it.
     *
     * @param httpRequest HTTP-based request to convert
     * @return protocol-agnostic request
     * @throws InvalidRequestException if no mapper is found for request or
     *             mapping fails
     */
    public Request map(HttpRequest httpRequest) throws InvalidRequestException {
        return findRequestMapper(httpRequest).parse(httpRequest);
    }

}
