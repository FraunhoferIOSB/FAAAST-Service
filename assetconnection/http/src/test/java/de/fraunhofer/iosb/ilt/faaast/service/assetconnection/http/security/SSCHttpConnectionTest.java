package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.security;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class SSCHttpConnectionTest {
    private static SSCHttpConnection sscHttpConnection = new SSCHttpConnection();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SSCHttpConnectionTest.class);

    private WireMockServer wireMockServer;
    private String trustStoreDirectoryPath = "assetconnection/http/src/test/resources/certificates";

    @BeforeAll
    public static void setUp() throws IOException {
        copyKeystoreToDestination("certificates/faaast.KEYSTORE.jks", sscHttpConnection.getTrustStorePath());
        sscHttpConnection.setTrustStorePassword("changeit".toCharArray());
    }

    @BeforeEach
    public void setup() {
        LOGGER.warn(sscHttpConnection.getTrustStorePath());
        this.wireMockServer = new WireMockServer(options()
                        .httpDisabled(true)
                        .httpsPort(8443)
                        .keystorePath(sscHttpConnection.getTrustStorePath())
                        .keystorePassword("changeit")
                        .keystoreType(SSCHttpConnection.JKS));
        this.wireMockServer.start();
        WireMock.configureFor(wireMockServer.httpsPort());
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    public void testSendRequest() throws Exception {
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
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals("Response", response.body());
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
