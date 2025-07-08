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

import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.AbstractEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.model.Interface;
import de.fraunhofer.iosb.ilt.faaast.service.model.Version;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.SecurityTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEndpoint;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProtocolInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSecurityAttributeObject;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.CrossOriginHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of HTTP endpoint. Accepts http request and maps them to Request objects passes them to the service and
 * expects a response object which is streamed as json response to the http client
 */
public class HttpEndpoint extends AbstractEndpoint<HttpEndpointConfig> {

    public static final Version API_VERSION = Version.V3_0;
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEndpoint.class);
    private static final CertificateInformation SELFSIGNED_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:endpoint:http")
            .commonName("FA³ST Service HTTP Endpoint")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .build();
    private static final String ENDPOINT_PROTOCOL = "HTTP";
    private static final String ENDPOINT_PROTOCOL_VERSION = "1.1";
    private Server server;
    private ServletContextHandler context;

    /**
     * Gets the API version prefix.
     *
     * @return the API version prefix
     */
    protected static String getVersionPrefix() {
        return String.format("/api/%s", API_VERSION);
    }


    @Override
    public void start() throws EndpointException {
        if (server != null && server.isStarted()) {
            return;
        }
        server = new Server();
        configureHttpServer();
        CrossOriginHandler crossOriginHandler = buildCorsHandler();
        server.setHandler(crossOriginHandler);

        context = new ServletContextHandler();
        context.setContextPath("/");
        crossOriginHandler.setHandler(context);

        RequestHandlerServlet handler = new RequestHandlerServlet(this, config, serviceContext);
        context.addServlet(handler, "/*");
        server.setErrorHandler(new HttpErrorHandler(config));
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
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();
        secureRequestCustomizer.setSniHostCheck(config.isSniEnabled());
        httpConfig.addCustomizer(secureRequestCustomizer);
        ServerConnector serverConnector;
        if (config.isSslEnabled()) {
            serverConnector = buildSSLServerConnector(httpConnectionFactory);
        }
        else {
            serverConnector = new ServerConnector(server, httpConnectionFactory);
            LOGGER.warn("Using HTTP endpoint with disabled SSL. Not safe for production - use for development only");
        }
        serverConnector.setPort(config.getPort());
        server.addConnector(serverConnector);
    }


    private CrossOriginHandler buildCorsHandler() {
        CrossOriginHandler result = new CrossOriginHandler();
        result.setAllowCredentials(config.isCorsAllowCredentials());
        result.setAllowedHeaders(Set.copyOf(HttpHelper.parseCommaSeparatedList(config.getCorsAllowedHeaders())));
        result.setAllowedMethods(Set.copyOf(HttpHelper.parseCommaSeparatedList(config.getCorsAllowedMethods())));
        result.setAllowedOriginPatterns(Set.copyOf(HttpHelper.parseCommaSeparatedList(config.getCorsAllowedOrigin())));
        result.setExposedHeaders(Set.copyOf(HttpHelper.parseCommaSeparatedList(config.getCorsExposedHeaders())));
        result.setPreflightMaxAge(Duration.ofMillis(config.getCorsMaxAge()));
        return result;
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
        if (context != null) {
            try {
                context.stop();
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


    @Override
    public List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> getAasEndpointInformation(String aasId) {
        if (Objects.isNull(server)) {
            return List.of();
        }
        List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> result = new ArrayList<>();
        if (config.getProfiles().stream()
                .flatMap(x -> x.getInterfaces().stream())
                .anyMatch(x -> Objects.equals(x, Interface.AAS_REPOSITORY))) {
            result.add(endpointFor("AAS-REPOSITORY-3.0", "/shells"));
        }
        if (config.getProfiles().stream()
                .flatMap(x -> x.getInterfaces().stream())
                .anyMatch(x -> Objects.equals(x, Interface.AAS))) {
            result.add(endpointFor("AAS-3.0", "/shells/" + EncodingHelper.base64UrlEncode(aasId)));
        }
        return result;
    }


    @Override
    public List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> getSubmodelEndpointInformation(String submodelId) {
        if (Objects.isNull(server)) {
            return List.of();
        }
        List<org.eclipse.digitaltwin.aas4j.v3.model.Endpoint> result = new ArrayList<>();
        if (config.getProfiles().stream()
                .flatMap(x -> x.getInterfaces().stream())
                .anyMatch(x -> Objects.equals(x, Interface.SUBMODEL_REPOSITORY))) {
            result.add(endpointFor("SUBMODEL-REPOSITORY-3.0", "/submodels"));
        }
        if (config.getProfiles().stream()
                .flatMap(x -> x.getInterfaces().stream())
                .anyMatch(x -> Objects.equals(x, Interface.SUBMODEL))) {
            result.add(endpointFor("SUBMODEL-3.0", "/submodels/" + EncodingHelper.base64UrlEncode(submodelId)));
        }

        return result;
    }


    private org.eclipse.digitaltwin.aas4j.v3.model.Endpoint endpointFor(String interfaceName, String path) {
        return new DefaultEndpoint.Builder()
                ._interface(interfaceName)
                .protocolInformation(new DefaultProtocolInformation.Builder()
                        .href(getEndpointUri().resolve(getVersionPrefix() + path).toASCIIString())
                        .endpointProtocol(ENDPOINT_PROTOCOL)
                        .endpointProtocolVersion(ENDPOINT_PROTOCOL_VERSION)
                        .securityAttributes(new DefaultSecurityAttributeObject.Builder()
                                .type(SecurityTypeEnum.NONE)
                                .key("")
                                .value("")
                                .build())
                        .build())
                .build();
    }


    private URI getEndpointUri() {
        URI result = server.getURI();
        if (Objects.nonNull(config.getHostname())) {
            try {
                result = new URI(
                        result.getScheme(),
                        result.getUserInfo(),
                        config.getHostname(),
                        result.getPort(),
                        result.getPath(),
                        result.getQuery(),
                        result.getFragment());
            }
            catch (URISyntaxException e) {
                LOGGER.warn("error creating endpoint URI for HTTP endpoint based on hostname from configuratoin (hostname: {})",
                        config.getHostname(),
                        e);
            }
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpEndpoint that = (HttpEndpoint) o;
        return super.equals(o)
                && Objects.equals(server, that.server)
                && Objects.equals(context, that.context);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), server, context);
    }
}
