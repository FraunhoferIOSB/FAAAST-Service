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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsResponse;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.Assert;


public class HttpTest {

    private static int findFreePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        Assert.assertNotNull(serverSocket);
        Assert.assertTrue(serverSocket.getLocalPort() > 0);
        return serverSocket.getLocalPort();
    }


    @org.junit.Test
    public void testServerBasics() throws Exception {
        int port = findFreePort();
        final String URL1 = "http://127.0.0.1:" + port + "/shellsXXX";
        final String URL2 = "http://127.0.0.1:" + port + "/shells";

        HttpEndpointConfig endpointConfig = new HttpEndpointConfig();
        endpointConfig.setPort(port);
        ServiceContext serviceContext = mock(ServiceContext.class);
        HttpEndpoint endpoint = new HttpEndpoint();
        endpoint.init(CoreConfig.builder()
                .build(),
                endpointConfig,
                serviceContext);
        endpoint.start();

        HttpClient client = new HttpClient();
        client.start();

        // incorrect URL
        // but we have no service yet
        // should get us a http/404 when service is finally present
        ContentResponse response = client.GET(URL1);
        Assert.assertTrue(response.getStatus() == HttpStatus.BAD_REQUEST_400);

        when(serviceContext.execute(any())).thenReturn(GetAllAssetAdministrationShellsResponse.builder()
                .statusCode(StatusCode.Success)
                .payload(List.of(AASFull.AAS_1))
                .build());
        // correct URL, but we have no service yet
        // should get us a http/200 when service is finally present
        response = client.GET(URL2);
        Assert.assertTrue(response.getStatus() == HttpStatus.OK_200);

        client.stop();

        endpoint.stop();
    }
}
