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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jetty.http.HttpStatus;


/**
 * Helper class providing HTTP specific functionality.
 */
public class HttpHelper {

    private HttpHelper() {}


    /**
     * Convert protocol-agnostic status code to HTTP-specific status code
     *
     * @param statusCode protocol-agnostic status code
     * @return HTTP-specific status code
     * @throws IllegalArgumentException if status code cannot be mapped to HTTP
     */
    public static int toHttpStatusCode(StatusCode statusCode) {
        switch (statusCode) {
            case SUCCESS:
                return HttpStatus.OK_200;
            case SUCCESS_CREATED:
                return HttpStatus.CREATED_201;
            case SUCCESS_NO_CONTENT:
                return HttpStatus.NO_CONTENT_204;
            case CLIENT_FORBIDDEN:
                return HttpStatus.FORBIDDEN_403;
            case CLIENT_ERROR_BAD_REQUEST:
                return HttpStatus.BAD_REQUEST_400;
            case CLIENT_METHOD_NOT_ALLOWED:
                return HttpStatus.METHOD_NOT_ALLOWED_405;
            case CLIENT_ERROR_RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND_404;
            case SERVER_INTERNAL_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR_500;
            case SERVER_ERROR_BAD_GATEWAY:
                return HttpStatus.BAD_GATEWAY_502;
            default:
                throw new IllegalArgumentException(String.format("unsupported status code '%s'", statusCode.name()));
        }
    }


    /**
     * Finds all methods of the url for which there is an implemented request mapper
     * registered to the given mapping manager
     *
     * @param mappingManager where the request mappers are registered
     * @param url which should be checked
     * @return all supported HTTP Methods of the given request
     */
    public static Set<HttpMethod> findSupportedHTTPMethods(RequestMappingManager mappingManager, String url) {
        Set<HttpMethod> allowedMethods = new HashSet<>();
        for (HttpMethod httpMethod: HttpMethod.values()) {
            try {
                HttpRequest httpRequest = HttpRequest.builder()
                        .path(url)
                        .method(httpMethod)
                        .build();
                mappingManager.findRequestMapper(httpRequest);
                allowedMethods.add(httpMethod);
            }
            catch (InvalidRequestException ignored) {
                //intentionally empty
            }
        }
        return allowedMethods;
    }
}
