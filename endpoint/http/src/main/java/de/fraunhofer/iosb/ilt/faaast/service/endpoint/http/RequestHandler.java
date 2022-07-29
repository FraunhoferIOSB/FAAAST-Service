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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseResponseWithPayload;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.RequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlets.CrossOriginFilter;


/**
 * HTTP handler that actually handles all requests to the endpoint by finding
 * the matching request class, deserializing the request, executing it using the
 * serviceContext and serializing the result.
 */
public class RequestHandler extends AbstractHandler {

    private static final int DEFAULT_PREFLIGHT_MAX_AGE = 1800;
    private final ServiceContext serviceContext;
    private final HttpEndpointConfig config;
    private final RequestMappingManager mappingManager;
    private final HttpJsonSerializer serializer;

    public RequestHandler(ServiceContext serviceContext, HttpEndpointConfig config) {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
        this.mappingManager = new RequestMappingManager(serviceContext);
        this.serializer = new HttpJsonSerializer();
    }


    @Override
    public void handle(String string, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                request.getInputStream().close();
            }
        };

        if (config.isCorsEnabled()) {
            setCORSHeader(response);
            if (isPreflightedCORSRequest(request)) {
                handlePreflightedCORSRequest(request, response, baseRequest);
                return;
            }
        }

        HttpRequest httpRequest = HttpRequest.builder()
                .path(request.getRequestURI().replaceAll("/$", ""))
                .query(request.getQueryString())
                .body(reader.lines().collect(Collectors.joining(System.lineSeparator())))
                .method(HttpMethod.valueOf(request.getMethod()))
                .headers(Collections.list(request.getHeaderNames()).stream()
                        .collect(Collectors.toMap(
                                x -> x,
                                x -> request.getHeader(x))))
                .build();
        de.fraunhofer.iosb.ilt.faaast.service.model.api.Request apiRequest = null;
        try {
            apiRequest = mappingManager.map(httpRequest);
        }
        catch (InvalidRequestException | IllegalArgumentException e) {
            sendError(response, StatusCode.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
            baseRequest.setHandled(true);
            return;
        }
        try {
            executeAndSend(response, apiRequest);
        }
        catch (SerializationException e) {
            sendException(response, StatusCode.SERVER_INTERNAL_ERROR, e.getMessage());
        }
        baseRequest.setHandled(true);
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
            Set<HttpMethod> allowedMethods = HttpHelper.findSupportedHTTPMethods(mappingManager, request.getRequestURI().replaceAll("/$", ""));
            allowedMethods.add(HttpMethod.OPTIONS);
            response.addHeader(CrossOriginFilter.ACCESS_CONTROL_ALLOW_METHODS_HEADER,
                    allowedMethods.stream()
                            .map(HttpMethod::name)
                            .collect(Collectors.joining(HttpConstants.HEADER_VALUE_SEPARATOR)));
            if (allowedMethods.containsAll(requestedHTTPMethods)) {
                response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, String.valueOf(DEFAULT_PREFLIGHT_MAX_AGE));
                sendSuccess(response, HttpStatus.NO_CONTENT_204);
            }
            else {
                sendError(response, StatusCode.CLIENT_ERROR_BAD_REQUEST);
            }
        }
        catch (RuntimeException e) {
            sendError(response, StatusCode.CLIENT_ERROR_BAD_REQUEST,
                    String.format("invalid value for %s: %s",
                            CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER,
                            request.getHeader(CrossOriginFilter.ACCESS_CONTROL_REQUEST_METHOD_HEADER)));
        }
        finally {
            baseRequest.setHandled(true);
        }
    }


    /**
     * Send result/error response which includes result object including message
     * object
     *
     * @param response http response object
     * @param statusCode http status code
     */
    private void sendFailure(HttpServletResponse response, StatusCode statusCode, Result result) {
        int httpStatusCode = HttpHelper.toHttpStatusCode(statusCode);
        try {
            sendJson(response, httpStatusCode, serializer.write(result));
        }
        catch (SerializationException e) {
            sendException(response, StatusCode.SERVER_INTERNAL_ERROR, e.getMessage());
        }
    }


    private void sendError(HttpServletResponse response, StatusCode statusCode) {
        sendError(response, statusCode, HttpStatus.getMessage(HttpHelper.toHttpStatusCode(statusCode)));
    }


    private void sendError(HttpServletResponse response, StatusCode statusCode, String message) {
        sendFailure(response, statusCode, Result.error(message));
    }


    private void sendException(HttpServletResponse response, StatusCode statusCode, String message) {
        sendFailure(response, statusCode, Result.exception(message));
    }


    private void executeAndSend(HttpServletResponse response, de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest)
            throws SerializationException {
        if (apiRequest == null) {
            sendError(response, StatusCode.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        Response apiResponse = serviceContext.execute(apiRequest);
        if (apiResponse == null) {
            sendException(response, StatusCode.SERVER_INTERNAL_ERROR, "apiResponse must be non-null");
            return;
        }
        int statusCode = HttpHelper.toHttpStatusCode(apiResponse.getStatusCode());
        if (apiResponse.getResult() != null && !apiResponse.getResult().getSuccess()) {
            sendJson(response, statusCode, serializer.write(apiResponse.getResult()));
        }
        else if (BaseResponseWithPayload.class.isAssignableFrom(apiResponse.getClass())) {
            try {
                Object payload = ((BaseResponseWithPayload) apiResponse).getPayload();
                if (RequestWithModifier.class.isAssignableFrom(apiRequest.getClass())) {
                    sendJson(response, statusCode, serializer.write(payload, ((RequestWithModifier) apiRequest).getOutputModifier()));
                }
                else {
                    sendJson(response, statusCode, serializer.write(((BaseResponseWithPayload) apiResponse).getPayload()));
                }
            }
            catch (SerializationException e) {
                sendException(response, StatusCode.SERVER_INTERNAL_ERROR, e.getMessage());
            }
        }
        else {
            sendSuccess(response, statusCode);
        }

    }


    private void sendSuccess(HttpServletResponse response, int statusCode) {
        sendSuccess(response, statusCode, null);
    }


    private void sendSuccess(HttpServletResponse response, int statusCode, String content) {
        send(response, statusCode, content, "text/plain");
    }


    private void sendJson(HttpServletResponse response, int statusCode, String content) {
        send(response, statusCode, content, "application/json");
    }


    private void send(HttpServletResponse response, int statusCode, String content, String contentType) {
        response.setStatus(statusCode);
        if (content != null) {
            PrintWriter out = null;
            try {
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(String.format("%s; charset=%s", contentType, StandardCharsets.UTF_8.name()));
                out = response.getWriter();
                out.print(content);
                out.flush();
            }
            catch (IOException e) {
                sendException(response, StatusCode.SERVER_INTERNAL_ERROR, e.getMessage());
            }
            finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }

}
