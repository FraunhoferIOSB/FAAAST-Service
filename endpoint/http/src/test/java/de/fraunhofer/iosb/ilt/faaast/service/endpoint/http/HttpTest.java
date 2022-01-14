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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpEndpoint;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Assert;


public class HttpTest {

    @org.junit.Test
    public void testServerBasics() throws Exception {
        final String URL1 = "http://127.0.0.1:8081/shellsXXX";
        final String URL2 = "http://127.0.0.1:8081/shells";

        HttpEndpoint httpEndpoint = new HttpEndpoint(8081);
        httpEndpoint.setService(null);
        httpEndpoint.start();

        HttpClient httpClient = new HttpClient();
        httpClient.start();

        // incorrect URL
        // but we have no service yet
        // should get us a http/404 when service is finally present
        ContentResponse response = httpClient.GET(URL1);
        Assert.assertTrue(response.getStatus() == 500);

        // correct URL, but we have no service yet
        // should get us a http/200 when service is finally present
        response = httpClient.GET(URL2);
        Assert.assertTrue(response.getStatus() == 500);

        httpClient.stop();

        httpEndpoint.stop();
    }
}
