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

import java.io.*;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against a list
 * of CA's and allow self-signed certificates to be authorized.
 */

public class SelfSignedCertificateHandler extends HttpClient {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    public static final String JKS = "JKS";
    private final SSLContext sslContext;
    private final SSLParameters sslParameters;
    private final Duration connectTimeout;
    private final Redirect followRedirects;
    private final Version version;
    private File truststoreFile;
    private final String TRUSTSTORE_PATH = "C:\\Users\\cha35985\\IdeaProjects\\FAAAST\\keystore\\truststore.jks";
    private final char[] TRUSTSTORE_PASSWORD = "changeit".toCharArray();

    /**
     * constructor.
     */
    public SelfSignedCertificateHandler(File truststoreFile) throws IOException {
        super();
        try {
            this.sslContext = createCustomSSLContext();
            this.sslParameters = sslContext.getDefaultSSLParameters();
        }
        catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create SSL context.", e);
        }
        this.connectTimeout = DEFAULT_TIMEOUT;
        this.followRedirects = Redirect.NORMAL;
        this.version = Version.HTTP_1_1;
        this.truststoreFile = truststoreFile;
        createTruststore();
    }


    private SSLContext createCustomSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}


                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        // For production use, it is crucial to perform proper certificate validation
                        // including checking the certificate chain, expiration, and other security criteria.
                        List<X509Certificate> trustedCerts = getTrustedCertificates();

                        for (X509Certificate cert: chain) {
                            // Check if the certificate is in the trusted certificates list and if self-signed
                            isSelfSigned(cert);
                            if (!trustedCerts.contains(cert)) {
                                throw new CertificateException("Certificate is not trusted.");
                            }
                        }
                    }


                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());

        return sslContext;
    }


    private List<X509Certificate> getTrustedCertificates() {
        try {
            // Load the keystore file containing the trusted certificates
            KeyStore trustStore = KeyStore.getInstance(JKS);
            FileInputStream fileInputStream = new FileInputStream(TRUSTSTORE_PATH);
            trustStore.load(fileInputStream, TRUSTSTORE_PASSWORD);

            // Iterate over the trusted certificates and add them to the list
            List<X509Certificate> trustedCerts = new ArrayList<>();
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) trustStore.getCertificate(alias);
                trustedCerts.add(cert);
            }

            return trustedCerts;
        }
        catch (Exception e) {
            //TODO- Handle the exception appropriately
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    private boolean isSelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }


    /**
     * create trust store.
     * 
     * @throws IOException
     */
    public void createTruststore() throws IOException {
        try {
            // Create a temporary file to store the truststore
            File truststoreFile = File.createTempFile("truststore", ".jks");

            try (InputStream inputStream = SelfSignedCertificateHandler.class.getResourceAsStream("/truststore.jks");
                    OutputStream os = new FileOutputStream(truststoreFile)) {
                // Copy the truststore resource to the temporary file
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            // Set the created truststore file as the class member
            this.truststoreFile = truststoreFile;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to create truststore.", e);
        }

    }


    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }


    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.of(connectTimeout);
    }


    @Override
    public Redirect followRedirects() {
        return followRedirects;
    }


    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }


    @Override
    public SSLContext sslContext() {
        return sslContext;
    }


    @Override
    public SSLParameters sslParameters() {
        return sslParameters;
    }


    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }


    @Override
    public Version version() {
        return version;
    }


    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }


    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .send(request, responseBodyHandler);
    }


    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler) {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .sendAsync(request, responseBodyHandler);
    }


    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler,
                                                            HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }


    /**
     * delete trust store file.
     */
    public void close() throws IOException {
        // Delete the truststore file
        deleteTruststore();
    }


    private void deleteTruststore() throws IOException {
        if (truststoreFile != null && truststoreFile.exists()) {
            Files.delete(truststoreFile.toPath());
        }
    }
}
