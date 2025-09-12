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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.ResponseMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.jetty.server.Response;


/**
 * HTTP handler that actually handles all requests to the endpoint by finding the matching request class, deserializing
 * the request, executing it using the serviceContext and serializing the result.
 */
public class RequestHandlerServlet extends HttpServlet {

    private final HttpEndpoint endpoint;
    private final HttpEndpointConfig config;
    private final ServiceContext serviceContext;
    private final RequestMappingManager requestMappingManager;
    private final ResponseMappingManager responseMappingManager;
    private final HttpJsonApiSerializer serializer;

    public RequestHandlerServlet(HttpEndpoint endpoint, HttpEndpointConfig config, ServiceContext serviceContext) {
        Ensure.requireNonNull(endpoint, "endpoint must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.endpoint = endpoint;
        this.config = config;
        this.serviceContext = serviceContext;
        this.requestMappingManager = new RequestMappingManager(serviceContext);
        this.responseMappingManager = new ResponseMappingManager(serviceContext);
        this.serializer = new HttpJsonApiSerializer();
    }


    private void doThrow(Exception e) throws ServletException {
        throw new ServletException(e);
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(HttpEndpoint.getVersionPrefix())) {
            doThrow(new ResourceNotFoundException(String.format("Resource not found '%s'", request.getRequestURI())));
        }
        String url = request.getRequestURI().replaceFirst(HttpEndpoint.getVersionPrefix(), "");
        HttpMethod method = null;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        }
        catch (IllegalArgumentException e) {
            doThrow(new MethodNotAllowedException(
                    String.format("Unknown method '%s'", request.getMethod()),
                    e));
        }
        HttpRequest httpRequest = HttpRequest.builder()
                .path(url.replaceAll("/$", ""))
                .query(request.getQueryString())
                .body(request.getInputStream().readAllBytes())
                .method(method)
                .charset(request.getCharacterEncoding())
                .headers(Collections.list(request.getHeaderNames()).stream()
                        .collect(Collectors.toMap(
                                x -> x,
                                request::getHeader)))
                .build();
        try {
            executeAndSend(response, requestMappingManager.map(httpRequest));
        }
        catch (Exception e) {
            doThrow(e);
        }

    }


    private void checkRequestSupportedByProfiles(de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest) throws InvalidRequestException {
        if (Objects.isNull(config.getProfiles()) || config.getProfiles().isEmpty()) {
            return;
        }
        config.getProfiles().stream()
                .flatMap(x -> x.getSupportedRequests().stream())
                .filter(x -> Objects.equals(x, apiRequest.getClass()))
                .findAny()
                .orElseThrow(() -> new InvalidRequestException(String.format(
                        "'%s' not supported on this server",
                        apiRequest.getClass().getSimpleName())));
    }


    private void executeAndSend(HttpServletResponse response,
                                de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest)
            throws Exception {
        if (Objects.isNull(apiRequest)) {
            throw new InvalidRequestException("empty API request");
        }
        checkRequestSupportedByProfiles(apiRequest);
        de.fraunhofer.iosb.ilt.faaast.service.model.api.Response apiResponse = serviceContext.execute(endpoint, apiRequest);
        if (Objects.isNull(apiResponse)) {
            throw new ServletException("empty API response");
        }
        if (isSuccessful(apiResponse)) {
            responseMappingManager.map(apiRequest, apiResponse, response);
        }
        else {
            HttpHelper.sendJson(response, apiResponse.getStatusCode(), serializer.write(apiResponse.getResult()));
        }
    }


    private static boolean isSuccessful(de.fraunhofer.iosb.ilt.faaast.service.model.api.Response response) {
        return Objects.nonNull(response)
                && response.getStatusCode().isSuccess()
                && Objects.nonNull(response.getResult())
                && Optional.ofNullable(response.getResult().getMessages())
                        .orElse(List.of())
                        .stream()
                        .map(message -> message.getMessageType())
                        .noneMatch(x -> Objects.equals(x, MessageTypeEnum.ERROR) || Objects.equals(x, MessageTypeEnum.EXCEPTION));
    }

}
