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
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import org.eclipse.jetty.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements HttpEndpoint accepts http request and maps them to Request objects
 * passes them to the service and expects a response object which is streamed as
 * json response to the http client
 */
public class HttpEndpoint implements Endpoint<HttpEndpointConfig> {

    private static Logger logger = LoggerFactory.getLogger(HttpEndpoint.class);
    private HttpEndpointConfig config;
    private ServiceContext serviceContext;
    private Server server;
    private Handler handler;

    @Override
    public HttpEndpointConfig asConfig() {
        return config;
    }


    @Override
    public void init(CoreConfig core, HttpEndpointConfig config, ServiceContext serviceContext) {
        if (config == null) {
            throw new IllegalArgumentException("config must be non-null");
        }
        if (serviceContext == null) {
            throw new IllegalArgumentException("service context must be non-null");
        }
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public void start() throws Exception {
        if (server != null && server.isStarted()) {
            return;
        }
        server = new Server(config.getPort());
        handler = new RequestHandler(serviceContext);
        server.setHandler(handler);
        server.setErrorHandler(new ErrorHandler());
        server.start();
    }


    @Override
    public void stop() {
        if (handler != null) {
            try {
                handler.stop();
            }
            catch (Exception ex) {
                logger.debug("stopping HTTP handler failed", ex);
            }
        }
        try {
            server.stop();
            server.join();
        }
        catch (Exception ex) {
            logger.debug("HTTP endpoint did non shutdown correctly", ex);
        }
    }
}
