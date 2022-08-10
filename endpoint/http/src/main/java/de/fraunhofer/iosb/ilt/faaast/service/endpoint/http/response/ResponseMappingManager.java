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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper.AbstractResponseMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps a given API response to HTTP by finding the best suited (most specific)
 * response mapper.
 */
public class ResponseMappingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestMappingManager.class);
    private List<AbstractResponseMapper> mappers;
    protected ServiceContext serviceContext;

    public ResponseMappingManager(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        init();
    }


    private void init() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            mappers = scanResult
                    .getSubclasses(AbstractResponseMapper.class.getName())
                    .filter(x -> !x.isAbstract() && !x.isInterface())
                    .loadClasses(AbstractResponseMapper.class)
                    .stream()
                    .map(x -> {
                        try {
                            Constructor<AbstractResponseMapper> constructor = x.getConstructor(ServiceContext.class);
                            return constructor.newInstance(serviceContext);
                        }
                        catch (NoSuchMethodException | SecurityException e) {
                            LOGGER.warn("response mapper implementation could not be loaded, "
                                    + "reason: missing constructor (implementation class: {}, required constructor signature: {})",
                                    x.getName(),
                                    ServiceContext.class.getName(),
                                    e);
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            LOGGER.warn("response mapper implementation could not be loaded, "
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
     * Maps a given API response to HTTP by finding the best suited (most
     * specific) response mapper.
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
                .sorted(Comparator.comparing(x -> x.getValue(), new MostSpecificClassComparator()))
                .map(x -> x.getKey())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("no matching response mapper found for type '%s'", apiResponse.getClass())))
                .map(apiRequest, apiResponse, httpResponse);
    }

}
