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

import static org.mockito.Mockito.*;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import java.util.List;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;


public class HttpEndpointSSLTest extends AbstractHttpEndpointTest {

    @Override
    protected void startServer() throws Exception {
        scheme = HttpScheme.HTTPS.toString();

        endpoint = new HttpEndpoint();
        server = new Server();
        service = spy(new Service(CoreConfig.DEFAULT, persistence, mock(MessageBus.class), List.of(endpoint), List.of()));
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .keystorePath("src/test/resources/serverkeystore.jks")
                        .keystorePassword("password")
                        .build(),
                service);
        server.start();
        service.start();
    }


    @Override
    protected void startClient() throws Exception {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setKeyStorePath("src/test/resources/clientkeystore.jks");
        sslContextFactory.setKeyStorePassword("password");
        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        client = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        client.start();
    }
}
