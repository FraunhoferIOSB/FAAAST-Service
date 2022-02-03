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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.MappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.BaseResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.BaseResponseWithPayload;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;


/**
 * Implements HttpEndpoint accepts http reqeust and maps them to Request objects
 * passes them to the service and expects a response object which is streamed as
 * json response to the http client
 */
public class HttpEndpoint implements Endpoint<HttpEndpointConfig> {

    private static final int DEFAULT_PORT = 8081;
    private Server server;
    private int port;
    private Service service;
    Handler handler;

    @Override
    public void init(CoreConfig core,  HttpEndpointConfig config, ServiceContext context) {}


    public HttpEndpoint() {
        this.port = DEFAULT_PORT;
    }


    public HttpEndpoint(int port) {
        this.port = port;
    }


    @Override
    public void setService(Service service) {
        this.service = service;
    }


    @Override
    public void start() throws Exception {
        if (server != null && server.isStarted()) {
            throw new IllegalStateException("HttpEndpoint cannot be started because it is already running");
        }
        server = new Server(port);
        handler = new Handler();
        handler.setService(service);
        server.setHandler(handler);
        server.setErrorHandler(new SimpleErrorHandler());
        try {
            server.start();
        }
        catch (Exception ex) {
            Logger.getLogger(HttpEndpoint.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("HTTP server could not be started", ex);
        }
    }


    @Override
    public void stop() {
        try {
            server.stop();
            server.join();
        }
        catch (Exception ex) {
            Logger.getLogger(HttpEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    @Override
    public HttpEndpointConfig asConfig() {
        return null;
    }

}


class Handler extends AbstractHandler {

    private MappingManager mappingManager = null;
    private Service service;

    public void setService(Service service) {
        this.service = service;
        this.mappingManager = new MappingManager();
    }


    @Override
    public void handle(String s, org.eclipse.jetty.server.Request baseRequest,
                       jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response)
            throws IOException, jakarta.servlet.ServletException {
        try {
            String url = request.getRequestURI();
            HttpRequest httpRequest = HttpRequest.builder()
                    .path(url)
                    .query(request.getQueryString())
                    .body(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())))
                    .build();
            httpRequest.setHeaders(
                    Collections.list(request.getHeaderNames()).stream()
                            .collect(Collectors.toMap(
                                    x -> x,
                                    x -> request.getHeader(x))));
            httpRequest.setMethod(HttpMethod.valueOf(request.getMethod()));
            Request apiRequest = mappingManager.map(httpRequest);
            if (service == null) {
                sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No Service Instance");
            }
            else if (apiRequest == null) {
                sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "");
            }
            else {
                Response apiResponse = service.execute(apiRequest);
                StatusCode statuscode = ((BaseResponse) apiResponse).getStatusCode();
                String content = "";
                if (BaseResponseWithPayload.class.isAssignableFrom(apiResponse.getClass())) {
                    // TODO serialize 
                    //                    content = IdGenerator.ResponseWriter(apiResponse);
                }
                sendResponse(response, statuscode.getStatusCode(), content);
            }
        }
        catch (Exception ex) {
            baseRequest.setHandled(true);
            throw new jakarta.servlet.ServletException(ex);
        }
        baseRequest.setHandled(true);
    }


    private void sendResponse(jakarta.servlet.http.HttpServletResponse response,
                              int statusCode, String content)
            throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(content);
        out.flush();
    }

}


class SimpleErrorHandler extends ErrorHandler {

    @Override
    public void handle(String target, org.eclipse.jetty.server.Request baseRequest, jakarta.servlet.http.HttpServletRequest request,
                       jakarta.servlet.http.HttpServletResponse response)
            throws IOException, jakarta.servlet.ServletException {
        super.handle(target, baseRequest, request, response);
        response.getWriter().write(
                String.format("{ 'returnCode': %d, 'reason': '%s'}",
                        response.getStatus(),
                        ((org.eclipse.jetty.server.Response) response).getReason()));
    }
}
