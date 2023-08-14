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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.server;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaConstants;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.SecurityPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigBuilder;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.CompositeValidator;
import org.eclipse.milo.opcua.sdk.server.identity.IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.X509IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.util.HostnameUtil;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaRuntimeException;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedHttpsCertificateBuilder;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.eclipse.milo.opcua.stack.server.security.DefaultServerCertificateValidator;
import org.eclipse.milo.opcua.stack.server.security.ServerCertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to run embedded OPC UA server
 */
public class EmbeddedOpcUaServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedOpcUaServer.class);
    public static final String DEFAULT_APPLICATION_CERTIFICATE_FILE = "server-application.p12";
    public static final String DEFAULT_APPLICATION_CERTIFICATE_PASSWORD = "";
    public static final String DEFAULT_APPLICATION_CERTIFICATE_KEY_STORE_TYPE = "PKCS12";
    private static final BuildInfo BUILD_INFO = BuildInfo.builder()
            .productUri("urn:de:fraunhofer:iosb:ilt:faaast:service:assetconnection:opcua:test")
            .manufacturerName("Fraunhofer IOSB")
            .productName("FA³ST OPC UA Asset Connection Test Server")
            .softwareVersion(OpcUaServer.SDK_VERSION)
            .buildDate(DateTime.now())
            .build();
    private static final List<TransportProfile> SUPPORTED_TRANSPORT_PROFILES = List.of(TransportProfile.HTTPS_UABINARY, TransportProfile.TCP_UASC_UABINARY);

    private final EmbeddedOpcUaServerConfig config;
    private final OpcUaServer server;
    private final List<ManagedNamespaceWithLifecycle> namespaces = new ArrayList<>();

    static {
        // Required for SecurityPolicy.Aes256_Sha256_RsaPss
        Security.addProvider(new BouncyCastleProvider());
    }

    public void allowClient(X509Certificate certificate) {
        config.getAllowedClientCertificates().add(certificate);
    }


    public void disallowClient(X509Certificate certificate) {
        config.getAllowedClientCertificates().remove(certificate);
    }


    public EmbeddedOpcUaServerConfig getConfig() {
        return config;
    }


    //    /**
    //     * Starts an embedded OPC UA server.
    //     *
    //     * @param identityValidator identity validator to use
    //     * @param tcpPort TCP port to use
    //     * @param httpsPort HTTPS port to use
    //     * @param endpointSecurityConfigurations information about which endpoints to create
    //     * @throws Exception if initialization of server fails
    //     */
    //    public EmbeddedOpcUaServer(
    //            IdentityValidator identityValidator,
    //            int tcpPort,
    //            int httpsPort,
    //            List<EndpointSecurityConfiguration> endpointSecurityConfigurations) throws Exception {
    //        this(identityValidator, tcpPort, httpsPort, Files.createTempDirectory("server"), endpointSecurityConfigurations);
    //    }
    private static IdentityValidator buildIdentityValidator(EmbeddedOpcUaServerConfig config) {
        Set<UserTokenType> availableTokenPolicies = config.getEndpointSecurityConfigurations().stream()
                .flatMap(x -> x.getTokenPolicies().stream())
                .distinct()
                .collect(Collectors.toSet());
        if (availableTokenPolicies.contains(UserTokenType.IssuedToken)) {
            throw new IllegalArgumentException(String.format("Unsupported user token policy: %s", UserTokenType.IssuedToken.name()));
        }
        if (availableTokenPolicies.isEmpty()) {
            throw new IllegalArgumentException("at least one user token policy is required");
        }
        BiFunction<IdentityValidator, IdentityValidator, IdentityValidator> compositor = (oldValidator, newValidator) -> Objects.isNull(oldValidator)
                ? newValidator
                : new CompositeValidator(oldValidator, newValidator);
        IdentityValidator result = null;
        if (availableTokenPolicies.contains(UserTokenType.Anonymous)) {
            result = compositor.apply(result, new AnonymousIdentityValidator());
        }
        if (availableTokenPolicies.contains(UserTokenType.UserName)) {
            result = compositor.apply(result, new UsernameIdentityValidator(
                    false,
                    x -> config.getAllowedCredentials().containsKey(x.getUsername())
                            && Objects.equals(x.getPassword(), config.getAllowedCredentials().get(x.getUsername()))));
        }
        if (availableTokenPolicies.contains(UserTokenType.Certificate)) {
            result = compositor.apply(result, new X509IdentityValidator(x -> config.getAllowedClientCertificates().contains(x)));
        }
        return result;
    }


    private static CertificateData getApplicationCertificate(EmbeddedOpcUaServerConfig config) throws IOException, GeneralSecurityException {
        return Objects.nonNull(config.getApplicationCertificate())
                ? config.getApplicationCertificate()
                : KeyStoreHelper
                        .loadOrDefaultCertificateData(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_APPLICATION_CERTIFICATE_FILE),
                                DEFAULT_APPLICATION_CERTIFICATE_KEY_STORE_TYPE,
                                null,
                                DEFAULT_APPLICATION_CERTIFICATE_PASSWORD,
                                DEFAULT_APPLICATION_CERTIFICATE_PASSWORD,
                                OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO);
    }


    private static CertificateData getHttpsCertificate(EmbeddedOpcUaServerConfig config) throws Exception {
        return Objects.nonNull(config.getHttpsCertificate())
                ? config.getHttpsCertificate()
                : generateHttpsCertificate();
    }


    private static CertificateData generateHttpsCertificate() throws Exception {
        KeyPair httpsKeyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
        SelfSignedHttpsCertificateBuilder httpsCertificateBuilder = new SelfSignedHttpsCertificateBuilder(httpsKeyPair)
                .setCommonName(HostnameUtil.getHostname());
        HostnameUtil.getHostnames("0.0.0.0").forEach(httpsCertificateBuilder::addDnsName);
        return CertificateData.builder()
                .keyPair(httpsKeyPair)
                .certificate(httpsCertificateBuilder.build())
                .build();
    }


    /**
     * Starts an embedded OPC UA server.
     *
     * @param config the config to start the server with
     * @throws Exception if initialization of server fails
     */
    public EmbeddedOpcUaServer(EmbeddedOpcUaServerConfig config) throws Exception {
        this.config = config;
        Files.createDirectories(config.getSecurityBaseDir());
        CertificateData applicationCertificate = getApplicationCertificate(config);
        CertificateData httpsCertificate = getHttpsCertificate(config);
        DefaultCertificateManager certificateManager = new DefaultCertificateManager(
                applicationCertificate.getKeyPair(),
                applicationCertificate.getCertificateChain());

        DefaultTrustListManager trustListManager = new DefaultTrustListManager(SecurityPathHelper.pki(config.getSecurityBaseDir()).toFile());
        ServerCertificateValidator certificateValidator = new DefaultServerCertificateValidator(trustListManager);

        // The configured application URI must match the one in the certificate(s)
        String applicationUri = CertificateUtil
                .getSanUri(applicationCertificate.getCertificate())
                .orElseThrow(() -> new UaRuntimeException(
                        StatusCodes.Bad_ConfigurationError,
                        "certificate is missing the application URI"));
        server = new OpcUaServer(new OpcUaServerConfigBuilder()
                .setEndpoints(createEndpointConfigurations(applicationCertificate.getCertificate(), config.getEndpointSecurityConfigurations()))
                .setApplicationUri(applicationUri)
                .setApplicationName(LocalizedText.english("FA³ST OPC UA Asset Connection Test Server"))
                .setBuildInfo(BUILD_INFO)
                .setCertificateManager(certificateManager)
                .setTrustListManager(trustListManager)
                .setCertificateValidator(certificateValidator)
                .setHttpsKeyPair(httpsCertificate.getKeyPair())
                .setHttpsCertificateChain(httpsCertificate.getCertificateChain())
                .setIdentityValidator(buildIdentityValidator(config))
                .setProductUri(BUILD_INFO.getProductUri())
                .build());
        namespaces.add(new ExampleNamespace(server));
        startupNamespaces();
    }


    private void startupNamespaces() {
        namespaces.forEach(x -> x.startup());
    }


    private void shutdownNamespaces() {
        namespaces.forEach(x -> x.shutdown());
    }


    private static UserTokenPolicy toPolicy(UserTokenType tokenType) {
        return UserTokenPolicy.builder()
                .policyId(tokenType.name())
                .tokenType(tokenType)
                .issuedTokenType(null)
                .issuerEndpointUrl(null)
                .securityPolicyUri(tokenType == UserTokenType.Anonymous ? null : SecurityPolicy.Basic256.getUri())
                .build();
    }


    private EndpointConfiguration.Builder applyEndpointSecurityConfiguration(EndpointConfiguration.Builder builder, EndpointSecurityConfiguration securityConfiguration) {
        return builder.copy()
                .setSecurityPolicy(securityConfiguration.getPolicy())
                .setSecurityMode(securityConfiguration.getSecurityMode())
                .addTokenPolicies(securityConfiguration.getTokenPolicies().stream()
                        .map(x -> toPolicy(x))
                        .toArray(UserTokenPolicy[]::new))
                .setTransportProfile(getTransportProfile(securityConfiguration.getProtocol()))
                .setBindPort(config.getProtocolPorts().get(securityConfiguration.getProtocol()));
    }


    public static TransportProfile getTransportProfile(Protocol protocol) {
        switch (protocol) {
            case TCP:
                return TransportProfile.TCP_UASC_UABINARY;
            case HTTPS:
                return TransportProfile.HTTPS_UABINARY;
            default:
                throw new IllegalStateException(String.format("unsupported protocol: %s", protocol));
        }
    }


    private static Set<String> getRelevantHostnames(X509Certificate certificate) {
        Set<String> result = new HashSet<>();
        result.addAll(CertificateUtil.getSanIpAddresses(certificate));
        result.addAll(CertificateUtil.getSanDnsNames(certificate));
        if (result.isEmpty()) {
            result.add(HostnameUtil.getHostname());
            result.addAll(HostnameUtil.getHostnames("0.0.0.0"));
        }
        return result;
    }


    public String getEndpoint(TransportProfile transportProfile) {
        Ensure.requireNonNull(transportProfile, "transportProfile must be non-null");
        if (!SUPPORTED_TRANSPORT_PROFILES.contains(transportProfile)) {
            throw new UnsupportedOperationException(String.format(
                    "transport profile not supported (transport profile: %s, supported transport profiles: %s)",
                    transportProfile,
                    SUPPORTED_TRANSPORT_PROFILES.stream().map(x -> x.name()).collect(Collectors.joining(", "))));
        }
        return String.format("%s://localhost:%s%s",
                transportProfile.getScheme(),
                config.getProtocolPorts().get(Protocol.from(transportProfile)),
                config.getPath());
    }


    public String getEndpoint(Protocol protocol) {
        return getEndpoint(getTransportProfile(protocol));
    }


    private Set<EndpointConfiguration> createEndpointConfigurations(X509Certificate certificate, List<EndpointSecurityConfiguration> endpointSecurityConfigurations) {
        List<EndpointSecurityConfiguration> securityConfigurations = endpointSecurityConfigurations;
        if (Objects.isNull(securityConfigurations) || securityConfigurations.isEmpty()) {
            LOGGER.warn("No endpoint security configuration specified - using default (all allowed configurations)");
            securityConfigurations = EndpointSecurityConfiguration.ALL;
        }
        Set<EndpointConfiguration> result = new HashSet<>();
        for (var hostname: getRelevantHostnames(certificate)) {
            EndpointConfiguration.Builder builder = EndpointConfiguration.newBuilder()
                    .setBindAddress("0.0.0.0")
                    .setHostname(hostname)
                    .setPath(config.getPath())
                    .setCertificate(certificate);
            for (var securityConfiguration: securityConfigurations) {
                result.add(applyEndpointSecurityConfiguration(builder, securityConfiguration).build());
            }

            EndpointConfiguration.Builder discoveryBuilder = builder.copy()
                    .setPath(String.format("%s/discovery", config.getPath()))
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None)
                    .addTokenPolicies(
                            toPolicy(UserTokenType.Anonymous),
                            toPolicy(UserTokenType.UserName),
                            toPolicy(UserTokenType.Certificate));
            if (config.getProtocolPorts().containsKey(Protocol.TCP)) {
                result.add(discoveryBuilder
                        .copy()
                        .setTransportProfile(TransportProfile.TCP_UASC_UABINARY)
                        .setBindPort(config.getProtocolPorts().get(Protocol.TCP))
                        .build());
            }
            if (config.getProtocolPorts().containsKey(Protocol.HTTPS)) {
                result.add(discoveryBuilder
                        .copy()
                        .setTransportProfile(TransportProfile.HTTPS_UABINARY)
                        .setBindPort(config.getProtocolPorts().get(Protocol.HTTPS))
                        .build());
            }
        }
        return result;
    }


    public OpcUaServer getServer() {
        return server;
    }


    public void startup() throws InterruptedException, ExecutionException {
        server.startup().get();
    }


    public void shutdown() throws InterruptedException, ExecutionException {
        shutdownNamespaces();
        server.shutdown().get();
    }
}
