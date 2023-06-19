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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.security;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import javax.net.ssl.SSLHandshakeException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SSCHttpConnectionTest {
    private SSCHttpConnection sscHttpConnection;
    private WireMockServer wireMockServer;

    // dirty fix: change it to @BeforeAll, reinitialising sscHttpConnection and calling copyKeystoreToDestination multiple times
    // BUT: don't do this, because it should also work without reinitialising
    @Before
    public void beforeEach() throws IOException {
        sscHttpConnection = new SSCHttpConnection();
        sscHttpConnection.setTrustStorePassword("changeit".toCharArray());
        copyKeystoreToDestination("certificates/faaast.KEYSTORE.jks", sscHttpConnection.getTrustStorePath());
    }


    @After
    public void tearDown() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }


    @Test
    public void testSendRequest() throws Exception {
        this.wireMockServer = new WireMockServer(options()
                .httpDisabled(true)
                .httpsPort(8443)
                .keystorePath(sscHttpConnection.getTrustStorePath())
                .keystorePassword("changeit")
                .keystoreType(SSCHttpConnection.JKS));
        this.wireMockServer.start();
        WireMock.configureFor(wireMockServer.httpsPort());

        // Configure WireMock to respond with a sample HTTP response
        wireMockServer.stubFor(get(WireMock.urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Response")));

        // Create an HTTP request to the WireMock server
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://127.0.0.1:" + wireMockServer.httpsPort() + "/"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // Send the request and receive the response
        HttpResponse<String> response = sendRequestWithSSLContext(request);

        // Perform assertions on the response
        Assert.assertEquals(200, response.statusCode());
        Assert.assertEquals("Response", response.body());

        HttpResponse<String> response2 = sendRequestWithSSLContext(request);

        // Perform assertions on the response
        Assert.assertEquals(200, response2.statusCode());
        Assert.assertEquals("Response", response2.body());
    }


    @Test
    public void testUntrustedCertificate() throws URISyntaxException {
        this.wireMockServer = new WireMockServer(options()
                .httpDisabled(true)
                .httpsPort(8443));
        this.wireMockServer.start();
        WireMock.configureFor(wireMockServer.httpsPort());

        // Configure WireMock to respond with a sample HTTP response
        wireMockServer.stubFor(get(WireMock.urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Response")));

        // Create an HTTP request to the WireMock server
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://127.0.0.1:" + wireMockServer.httpsPort() + "/"))
                .timeout(Duration.ofSeconds(5))
                .build();

        // Send the request and expect SSLHandshakeException
        Assert.assertThrows(SSLHandshakeException.class, () -> sendRequestWithSSLContext(request));
    }


    private HttpResponse<String> sendRequestWithSSLContext(HttpRequest request) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .sslContext(sscHttpConnection.createCustomSSLContext())
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }


    private static void copyKeystoreToDestination(String source, String destination) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(source);
        File file = new File(url.getPath());

        byte[] bytes = Files.readAllBytes(file.toPath());
        Files.write(Path.of(destination), bytes);
    }
}
