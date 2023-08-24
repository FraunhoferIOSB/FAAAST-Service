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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.StreamHelper;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Exception to indicate a given method is not allowed for the URL.
 */
public class MethodNotAllowedException extends InvalidRequestException {

    public MethodNotAllowedException(HttpRequest request, HttpMethod... allowedMethods) {
        super(String.format("method '%s' not allowed for URL '%s' (allowed methods: %s)",
                request.getMethod(),
                request.getPath(),
                StreamHelper.toStream(allowedMethods)
                        .map(Enum::name)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", "))));
    }


    public MethodNotAllowedException(HttpRequest request, Collection<AbstractRequestMapper> allowedMethodMappers) {
        this(request, allowedMethodMappers.stream()
                .map(x -> x.getMethod())
                .toArray(HttpMethod[]::new));
    }
}
