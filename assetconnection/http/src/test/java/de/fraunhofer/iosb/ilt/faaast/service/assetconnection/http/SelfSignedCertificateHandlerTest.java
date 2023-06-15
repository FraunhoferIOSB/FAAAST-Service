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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.security.SelfSignedCertificateHandler;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Objects;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class SelfSignedCertificateHandlerTest {

    @Mock
    private HttpClient mockHttpClient;

    @InjectMocks
    private SelfSignedCertificateHandler certificateHandler;

    private static File truststoreFile;

    @BeforeAll
    public static void setUpClass() throws Exception {
        // Create a temporary truststore file
        Path tempTruststore = Files.createTempFile("truststore", ".jks");
        truststoreFile = tempTruststore.toFile();

        // Copy the truststore file to the temporary file
        Files.copy(
                Objects.requireNonNull(SelfSignedCertificateHandlerTest.class.getResourceAsStream("/truststore.jks")),
                tempTruststore,
                StandardCopyOption.REPLACE_EXISTING);
    }


    @AfterAll
    public static void tearDownClass() {
        // Delete the temporary truststore file
        truststoreFile.delete();
    }


    @BeforeEach
    public void setUp() throws IOException {
        certificateHandler = new SelfSignedCertificateHandler(truststoreFile);
    }


    @AfterEach
    public void tearDown() {
        // Reset mocks and clear invocations
        Mockito.reset(mockHttpClient);
    }


    @Test
    public void send_ShouldInvokeHttpClientSendMethod() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://localhost:8080"))
                .timeout(Duration.ofSeconds(5))
                .build();

        certificateHandler.send(request, HttpResponse.BodyHandlers.ofString());
        verify(mockHttpClient).send(request, HttpResponse.BodyHandlers.ofString());
    }


    @Test
    public void sendAsync_ShouldInvokeHttpClientSendAsyncMethod() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8080"))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

        verify(mockHttpClient).sendAsync(request, responseBodyHandler);
    }


    @Test
    public void sendAsync_WithPushPromiseHandler_ShouldInvokeHttpClientSendAsyncMethod() {
        // Arrange
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8080"))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();
        HttpResponse.PushPromiseHandler<String> pushPromiseHandler = Mockito.mock(HttpResponse.PushPromiseHandler.class);

        certificateHandler.sendAsync(request, responseBodyHandler, pushPromiseHandler);
        verify(mockHttpClient).sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }


    @Test
    public void close_ShouldDeleteTruststoreFile() throws Exception {
        // Act
        certificateHandler.close();
        Assertions.assertFalse(truststoreFile.exists());
    }


    @Test
    public void close_WithIOException_ShouldNotThrowException() throws Exception {

        Files.delete(truststoreFile.toPath()); // Simulate file already deleted
        Assertions.assertDoesNotThrow(() -> certificateHandler.close());
    }


    @Test
    public void createTruststore_ShouldCreateTruststoreFile() throws Exception {

        File truststoreFile = mock(File.class);
        whenNew(File.class).withAnyArguments().thenReturn(truststoreFile);
        certificateHandler.createTruststore();
        verify(truststoreFile).createNewFile();
    }


    @Test
    public void createTruststore_WithIOException_ShouldThrowException() throws Exception {

        doThrow(new java.io.IOException()).when(mockHttpClient).send(any(), any());

        Assertions.assertThrows(IOException.class, () -> certificateHandler.createTruststore());
    }
}
