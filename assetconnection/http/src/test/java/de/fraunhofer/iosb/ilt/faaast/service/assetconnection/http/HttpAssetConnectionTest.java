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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentFormat;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class HttpAssetConnectionTest {
    //private static final Reference DEFAULT_REFERENCE = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
    private static final String LOCALHOST = "127.0.0.1";
    private static int httpPort;
    private static HttpServer httpServer;
    private static String httpServerUri;
    private static int value = 5;

    @AfterClass
    public static void cleanup() throws IOException {
        httpServer.stop(0);
    }


    @BeforeClass
    public static void init() throws IOException {
        try {
            httpPort = findFreePort();
        }
        catch (IOException e) {
            Assert.fail("could not find free port");
        }
        httpServer = HttpServer.create(new InetSocketAddress(LOCALHOST, httpPort), 0);
        httpServerUri = "http://" + LOCALHOST + ":" + httpPort;
        httpServer.createContext("/test", new TestHttpHandler());
        httpServer.start();
    }

    /**
     * simple HTTP test handler, that returns a single Integer value
     */
    private static class TestHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            OutputStream outputStream = httpExchange.getResponseBody();
            if (httpExchange.getRequestMethod().equals("PUT")) {
                String ascii = Character.toString(httpExchange.getRequestBody().read());
                value = Integer.valueOf(ascii);
            }
            String response = "{\"value\":" + value + "}";
            httpExchange.sendResponseHeaders(200, response.length());
            outputStream.write(response.getBytes());
            outputStream.flush();
            outputStream.close();
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Assert.assertNotNull(serverSocket);
            Assert.assertTrue(serverSocket.getLocalPort() > 0);
            return serverSocket.getLocalPort();
        }
    }


    @Test
    public void testValueProviderProperty()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        Reference reference = AasUtils.parseReference("(Property)[ID_SHORT]Temperature");
        ServiceContext serviceContext = mock(ServiceContext.class);

        PropertyValue expected = PropertyValue.of(Datatype.INT, "5");
        doReturn(ElementValueTypeInfo.builder()
                .type(expected.getClass())
                .datatype(expected.getValue().getDataType())
                .build())
                        .when(serviceContext)
                        .getTypeInfo(reference);
        HttpAssetConnection connection = new HttpAssetConnection(
                CoreConfig.builder()
                        .build(),
                HttpAssetConnectionConfig.builder()
                        .valueProvider(reference,
                                HttpValueProviderConfig.builder()
                                        .path("/test")
                                        .contentFormat(ContentFormat.JSON)
                                        .build())
                        .serverUri(httpServerUri)
                        .build(),
                serviceContext);

        //get value
        DataElementValue actual = connection.getValueProviders().get(reference).getValue();
        Assert.assertEquals(expected, actual);

        //change value
        PropertyValue change = PropertyValue.of(Datatype.INT, "8");
        connection.getValueProviders().get(reference).setValue(change);
        DataElementValue changed = connection.getValueProviders().get(reference).getValue();
        Assert.assertEquals(change, changed);
        connection.close();
    }

}
