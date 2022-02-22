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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseResponseWithPayload;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.RequestWithModifier;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


/**
 * HTTP handler that actually handles all requests to the endpoint by finding
 * the matching request class, deserializing the request, executing it using the
 * serviceContext and serializing the result.
 */
public class RequestHandler extends AbstractHandler {

    private ServiceContext serviceContext;
    private RequestMappingManager mappingManager;
    private HttpJsonSerializer serializer;

    public RequestHandler(ServiceContext serviceContext) {
        if (serviceContext == null) {
            throw new IllegalArgumentException("serviceContext must be non-null");
        }
        this.serviceContext = serviceContext;
        this.mappingManager = new RequestMappingManager(serviceContext);
        this.serializer = new HttpJsonSerializer();
    }


    @Override
    public void handle(String string, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8")) {
            @Override
            public void close() throws IOException {
                request.getInputStream().close();
            }
        };

        HttpRequest httpRequest = HttpRequest.builder()
                .path(request.getRequestURI())
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
        catch (InvalidRequestException | IllegalArgumentException ex) {
            send(response, HttpStatus.BAD_REQUEST_400, ex.getMessage());
        }
        //TODO more differentiated error codes (must be generated in mappingManager)
        executeAndSend(response, apiRequest);
        baseRequest.setHandled(true);
    }


    private void executeAndSend(HttpServletResponse response, de.fraunhofer.iosb.ilt.faaast.service.model.api.Request apiRequest) throws IOException {
        // TODO forward output modifier to serializer
        if (apiRequest == null) {
            send(response, HttpStatus.BAD_REQUEST_400);
            return;
        }
        Response apiResponse = serviceContext.execute(apiRequest);
        if (apiResponse == null) {
            send(response, HttpStatus.INTERNAL_SERVER_ERROR_500);
            return;
        }
        int statusCode = HttpHelper.toHttpStatusCode(apiResponse.getStatusCode());
        if (BaseResponseWithPayload.class.isAssignableFrom(apiResponse.getClass())) {
            try {
                if (RequestWithModifier.class.isAssignableFrom(apiRequest.getClass())) {
                    Object payload = ((BaseResponseWithPayload) apiResponse).getPayload();
                    OutputModifier outputModifier = ((RequestWithModifier) apiRequest).getOutputModifier();
                    sendJson(response, statusCode, serializer.write(payload, outputModifier));
                }
                else {
                    sendJson(response, statusCode, serializer.write(((BaseResponseWithPayload) apiResponse).getPayload()));
                }
            }
            catch (SerializationException ex) {
                send(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.getMessage());
            }
        }
        else {
            send(response, statusCode);
        }
    }


    private void send(HttpServletResponse response, int statusCode) throws IOException {
        send(response, statusCode, null);
    }


    private void send(HttpServletResponse response, int statusCode, String content) throws IOException {
        send(response, statusCode, content, "text/plain");
    }


    private void sendJson(HttpServletResponse response, int statusCode, String content) throws IOException {
        send(response, statusCode, content, "application/json");
    }


    private void send(HttpServletResponse response, int statusCode, String content, String contentType) throws IOException {
        response.setStatus(statusCode);
        if (content != null) {
            response.setCharacterEncoding("UTF-8");
            response.setContentType(contentType + "; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(content);
            out.flush();
        }
    }

}
