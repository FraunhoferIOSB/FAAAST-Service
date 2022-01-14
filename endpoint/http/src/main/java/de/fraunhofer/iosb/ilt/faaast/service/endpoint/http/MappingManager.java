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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds available RequestMappings
 */
public class MappingManager {

    private static Logger logger = LoggerFactory.getLogger(MappingManager.class);
    private List<RequestMapper> mappers;

    public MappingManager() {
        init();
    }


    private void init() {
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo() // Scan classes, methods, fields, annotations
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            mappers = scanResult
                    .getSubclasses(RequestMapper.class.getName())
                    .loadClasses(RequestMapper.class)
                    .stream()
                    .map(x -> {
                        try {
                            Constructor<RequestMapper> constructor = x.getConstructor();
                            return constructor.newInstance();
                        }
                        catch (NoSuchMethodException | SecurityException ex) {
                            logger.warn("request mapper implementation could not be loaded, "
                                    + "reason: missing constructor (implementation class: {}, required constructor signature: {}",
                                    x.getName());
                        }
                        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            logger.warn("request mapper implementation could not be loaded, "
                                    + "reason: calling constructor failed (implementation class: {}, constructor arguments: {}",
                                    x.getName());
                        }
                        return null;
                    })
                    .collect(Collectors.toList());

            // filter out all which are null
            mappers.stream()
                    .filter(x -> x == null)
                    .collect(Collectors.toList());
        }
    }


    public Request map(HttpRequest httpRequest) {
        if (httpRequest == null) {
            throw new IllegalArgumentException("httpRequest must be non-null");
        }
        Optional<RequestMapper> mapper = mappers.stream()
                .filter(request -> request.matches(httpRequest))
                .findAny();
        if (mapper.isEmpty()) {
            throw new IllegalArgumentException("no matching request mapper found");
        }
        return mapper.get().parse(httpRequest);
    }

}
