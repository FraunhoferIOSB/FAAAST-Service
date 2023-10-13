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

import static org.eclipse.jetty.servlets.CrossOriginFilter.ACCESS_CONTROL_MAX_AGE_HEADER;

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.ResponseMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;


/**
 * HTTP handler that actually handles all requests to the endpoint by finding the matching request class, deserializing
 * the request, executing it using the serviceContext and serializing the result.
 */
public class RequestHandler extends AbstractHandler {

    private static final int DEFAULT_PREFLIGHT_MAX_AGE = 1800;
    private final ServiceContext serviceContext;
    private final HttpEndpointConfig config;
    private final RequestMappingManager requestMappingManager;
    private final ResponseMappingManager responseMappingManager;
    private final HttpJsonApiSerializer serializer;

    public RequestHandler(ServiceContext serviceContext, HttpEndpointConfig config) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
        this.requestMappingManager = new RequestMappingManager(serviceContext);
        this.responseMappingManager = new ResponseMappingManager(serviceContext);
        this.serializer = new HttpJsonApiSerializer();
    }


    @Override
    public void handle(String string, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (config.isCorsEnabled()) {
            setCORSHeader(response);
            if (isPreflightedCORSRequest(request)) {
                handlePreflightedCORSRequest(request, response, baseRequest);
                return;
            }
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        }
        catch (IllegalArgumentException e) {
            HttpHelper.send(response, StatusCode.CLIENT_METHOD_NOT_ALLOWED, Result.error(String.format("Unknown method '%s'", request.getMethod())));
            baseRequest.setHandled(true);
            return;
        }

        HttpRequest httpRequest = HttpRequest.builder()
                .path(request.getRequestURI().replaceAll("/$", ""))
                .query(request.getQueryString())
                .body(request.getInputStream().readAllBytes())
                .method(method)
                .charset(request.getCharacterEncoding())
                .headers(Collections.list(request.getHeaderNames()).stream()
                        .collect(Collectors.toMap(
                                x -> x,
                                x -> request.getHeader(x))))
                .build();
        try {
            executeAndSend(response, requestMappingManager.map(httpRequest));
        }
        catch (MethodNotAllowedException e) {
            HttpHelper.send(response, StatusCode.CLIENT_METHOD_NOT_ALLOWED, Result.error(e.getMessage()));
        }
        catch (InvalidRequestException | IllegalArgumentException e) {
            HttpHelper.send(response, StatusCode.CLIENT_ERROR_BAD_REQUEST, Result.error(e.getMessage()));
        }
        catch (SerializationException | RuntimeException e) {
            HttpHelper.send(response, StatusCode.SERVER_INTERNAL_ERROR, Result.exception(e.getMessage()));
        }
        finally {
            baseRequest.setHandled(true);
        }
    }


    private void setCORSHeader(HttpServletResponse response) {
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_HEADERS_HEADER, "Content-Type");
    }


    private boolean isPreflightedCORSRequest(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name());
    }


    private void handlePreflightedCORSRequest(HttpServletRequest request, HttpServletResponse response, Request baseRequest) {
        try {
            Set<HttpMethod> requestedHTTPMethods = request.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER) != null
                    ? Stream.of(request.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER)
                            .split(HttpConstants.HEADER_VALUE_SEPARATOR))
                            .filter(StringUtils::isNoneBlank)
                            .map(x -> HttpMethod.valueOf(x.trim()))
                            .collect(Collectors.toSet())
                    : new HashSet<>();
            Set<HttpMethod> allowedMethods = requestMappingManager.getSupportedMethods(request.getRequestURI().replaceAll("/$", ""));
            allowedMethods.add(HttpMethod.OPTIONS);
            response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER,
                    allowedMethods.stream()
                            .map(HttpMethod::name)
                            .collect(Collectors.joining(HttpConstants.HEADER_VALUE_SEPARATOR)));
            if (allowedMethods.containsAll(requestedHTTPMethods)) {
                response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(DEFAULT_PREFLIGHT_MAX_AGE));
                HttpHelper.sendContent(response, StatusCode.SUCCESS_NO_CONTENT, null, MediaType.PLAIN_TEXT_UTF_8);
            }
            else {
                HttpHelper.send(response, StatusCode.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        catch (RuntimeException e) {
            HttpHelper.send(response, StatusCode.CLIENT_ERROR_BAD_REQUEST,
                    Result.exception(String.format("invalid value for %s: %s",
                            CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER,
                            request.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER))));
        }
        finally {
            baseRequest.setHandled(true);
        }
    }


    private void executeAndSend(HttpServletResponse response, de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest)
            throws SerializationException {
        if (apiRequest == null) {
            HttpHelper.send(response, StatusCode.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        Response apiResponse = serviceContext.execute(apiRequest);
        if (apiResponse == null) {
            HttpHelper.send(response, StatusCode.SERVER_INTERNAL_ERROR, Result.exception("empty API response"));
            return;
        }
        if (apiResponse.getResult() != null && !apiResponse.getResult().getSuccess()) {
            HttpHelper.sendJson(response, apiResponse.getStatusCode(), serializer.write(apiResponse.getResult()));
        }
        else {
            responseMappingManager.map(apiRequest, apiResponse, response);
        }
    }
}
