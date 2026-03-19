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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.util.List;
import java.util.UUID;
import org.eclipse.digitaltwin.aas4j.v3.model.Endpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.ProtocolInformation;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.server.Server;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class HttpEndpointWithExtendedEndpointInformationTest extends AbstractHttpEndpointTest {

    private static final String subprotocol = "EXAMPLE";
    private static final String subprotocolBody = "my-example-subprotocol-body";
    private static final String subprotocolBodyEncoding = "none";

    @BeforeClass
    public static void init() throws Exception {
        port = PortHelper.findFreePort();
        deserializer = new HttpJsonApiDeserializer();
        persistence = mock(Persistence.class);
        fileStorage = mock(FileStorage.class);
        startServer();
        startClient();
    }


    private static void startServer() throws Exception {
        scheme = HttpScheme.HTTP.toString();
        endpoint = new HttpEndpoint();
        server = new Server();
        service = spy(new Service(CoreConfig.DEFAULT, persistence, fileStorage, mock(MessageBus.class), List.of(endpoint), List.of(), List.of()));
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .ssl(false)
                        .build(),
                service);
        server.start();
        service.start();
    }


    private static void startClient() throws Exception {
        client = new HttpClient(new HttpClientTransportDynamic(new ClientConnector()));
        client.start();
    }


    @Test
    public void testGetAasEndpointInformationWithCallbackAddress() {
        String callbackAddress = "https://invalid.local:1234/path";
        endpoint.init(
                CoreConfig.DEFAULT,
                HttpEndpointConfig.builder()
                        .port(port)
                        .cors(true)
                        .ssl(false)
                        .callbackAddress(callbackAddress)
                        .subprotocol(subprotocol)
                        .subprotocolBody(subprotocolBody)
                        .subprotocolBodyEncoding(subprotocolBodyEncoding)
                        .hostname("http://willbeoverridden.local:4242/example")
                        .build(),
                service);

        String expectedHref = callbackAddress.concat(endpoint.getPathPrefix()).concat("/shells");
        List<Endpoint> actual = endpoint.getAasEndpointInformation(UUID.randomUUID().toString());

        assertProtocolInformation(actual.get(0).getProtocolInformation(), expectedHref);
    }


    private void assertProtocolInformation(ProtocolInformation protocolInformation, String expectedCallbackAddress) {
        Assert.assertEquals(expectedCallbackAddress, protocolInformation.getHref());
        Assert.assertEquals(subprotocol, protocolInformation.getSubprotocol());
        Assert.assertEquals(subprotocolBody, protocolInformation.getSubprotocolBody());
        Assert.assertEquals(subprotocolBodyEncoding, protocolInformation.getSubprotocolBodyEncoding());
    }
}
