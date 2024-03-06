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

import static de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper.DEFAULT_ALIAS;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Objects;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of HTTP endpoint. Accepts http request and maps them to Request objects passes them to the service and
 * expects a response object which is streamed as json response to the http client
 */
public class HttpEndpoint implements Endpoint<HttpEndpointConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEndpoint.class);
    private static final CertificateInformation SELFSIGNED_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:endpoint:http")
            .commonName("FA³ST Service HTTP Endpoint")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .build();
    private HttpEndpointConfig config;
    private ServiceContext serviceContext;
    private Server server;
    private Handler handler;

    @Override
    public HttpEndpointConfig asConfig() {
        return config;
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException is config is null
     */
    @Override
    public void init(CoreConfig coreConfig, HttpEndpointConfig config, ServiceContext serviceContext) {
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public void start() throws EndpointException {
        if (server != null && server.isStarted()) {
            return;
        }
        server = new Server();
        configureHttpServer();
        handler = new RequestHandler(serviceContext, config);
        server.setHandler(handler);
        server.setErrorHandler(new HttpErrorHandler());
        try {
            server.start();
        }
        catch (Exception e) {
            throw new EndpointException("error starting HTTP endpoint", e);
        }
    }


    private void configureHttpServer() throws EndpointException {
        HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setSendServerVersion(false);
        httpConfig.setSendDateHeader(false);
        httpConfig.setSendXPoweredBy(false);
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
        ServerConnector serverConnector;
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniHostCheck(config.isSniEnabled());
        httpConfig.addCustomizer(secureRequestCustomizer);
        serverConnector = buildSSLServerConnector(httpConnectionFactory);
        serverConnector.setPort(config.getPort());
        server.addConnector(serverConnector);
    }


    private KeyStore generateSelfSignedCertificate() throws EndpointException {
        try {
            LOGGER.debug("Generating self-signed certificate for HTTP endpoint...");
            CertificateData certificateData = KeyStoreHelper.generateSelfSigned(SELFSIGNED_CERTIFICATE_INFORMATION);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry(DEFAULT_ALIAS, certificateData.getCertificate());
            keyStore.setKeyEntry(DEFAULT_ALIAS, certificateData.getKeyPair().getPrivate(), null, certificateData.getCertificateChain());
            LOGGER.debug("Self-signed certificate for HTTP endpoint successfully generated");
            return keyStore;
        }
        catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new EndpointException("error generating self-signed certificate for HTTPS endpoint", e);
        }
    }


    private ServerConnector buildSSLServerConnector(HttpConnectionFactory httpConnectionFactory) throws EndpointException {
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        if (Objects.isNull(config.getCertificate())
                || Objects.isNull(config.getCertificate().getKeyStorePath())
                || config.getCertificate().getKeyStorePath().equals("")) {
            LOGGER.info("Generating self-signed certificate for HTTPS (reason: no certificate provided)");
            sslContextFactory.setKeyStore(generateSelfSignedCertificate());
        }
        else {
            try {
                KeyStore keyStore = KeyStoreHelper.load(
                        new File(config.getCertificate().getKeyStorePath()),
                        config.getCertificate().getKeyStoreType(),
                        config.getCertificate().getKeyStorePassword());
                sslContextFactory.setKeyStorePassword(config.getCertificate().getKeyStorePassword());
                sslContextFactory.setKeyManagerPassword(config.getCertificate().getKeyPassword());
                sslContextFactory.setKeyStore(keyStore);
            }
            catch (IOException | GeneralSecurityException e) {
                throw new EndpointException("Error loading certificate for HTTP endpoint", e);
            }
        }
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, httpConnectionFactory.getProtocol());
        return new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
    }


    @Override
    public void stop() {
        if (handler != null) {
            try {
                handler.stop();
            }
            catch (Exception e) {
                LOGGER.debug("stopping HTTP handler failed", e);
            }
        }
        try {
            server.stop();
            server.join();
        }
        catch (Exception e) {
            LOGGER.debug("HTTP endpoint did non shutdown correctly", e);
            Thread.currentThread().interrupt();
        }
    }
}
