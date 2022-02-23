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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.*;
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
import java.util.Date;
import java.util.stream.Collectors;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;


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
            //ex.printStackTrace();
            sendResultResponse(response, HttpStatus.BAD_REQUEST_400, ex.getMessage());
            baseRequest.setHandled(true);
            return;
        }
        //TODO more differentiated error codes (must be generated in mappingManager)
        try {
            executeAndSend(response, apiRequest);
        }
        catch (SerializationException e) {
            e.printStackTrace();
        }
        baseRequest.setHandled(true);
    }


    /**
     * Send result/error response which includes result object including message object
     *
     * @param response http response object
     * @param httpStatusCode http status code
     * @param errorMessage clear text error message
     */
    private void sendResultResponse(HttpServletResponse response, int httpStatusCode, String errorMessage) throws IOException {
        if (errorMessage.isEmpty())
            errorMessage = HttpStatus.getMessage(httpStatusCode);
        Message message = Message.builder()
                .text(errorMessage)
                .success(MessageType.Error)
                .code(HttpStatus.getMessage(httpStatusCode))
                .timestamp(new Date())
                .build();
        Result result = Result.builder()
                .message(message)
                .success(false)
                .build();
        try {
            sendJson(response, httpStatusCode, serializer.write(result));
        }
        catch (SerializationException ex) {
            send(response, httpStatusCode, errorMessage, "text/plain");
        }
    }


    private void executeAndSend(HttpServletResponse response, de.fraunhofer.iosb.ilt.faaast.service.model.api.Request apiRequest) throws IOException, SerializationException {
        // TODO forward output modifier to serializer
        if (apiRequest == null) {
            sendResultResponse(response, HttpStatus.BAD_REQUEST_400, "");
            return;
        }
        Response apiResponse = serviceContext.execute(apiRequest);
        if (apiResponse == null) {
            sendResultResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, "");
            return;
        }
        int statusCode = HttpHelper.toHttpStatusCode(apiResponse.getStatusCode());
        if (!apiResponse.getResult().getSuccess() || !HttpStatus.isSuccess(statusCode)) {
            sendResultResponse(response, statusCode, HttpStatus.getMessage((statusCode)));
        }
        else if (BaseResponseWithPayload.class.isAssignableFrom(apiResponse.getClass())) {
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
                sendResultResponse(response, HttpStatus.INTERNAL_SERVER_ERROR_500, ex.getMessage());
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
