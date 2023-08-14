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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.UaServiceFaultException;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for working with OPC UA connections.
 */
public class OpcUaHelper {

    public static final String NODE_ID_SEPARATOR = ";";
    public static final String APPLICATION_URI = "urn:de:fraunhofer:iosb:ilt:faaast:service:assetconnection:opcua";
    public static final String APPLICATION_NAME = "FA³ST Asset Connection";
    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaHelper.class);
    private static final List<TransportProfile> SUPPORTED_TRANSPORT_SCHEMES = List.of(
            TransportProfile.TCP_UASC_UABINARY,
            TransportProfile.HTTPS_UABINARY,
            TransportProfile.WSS_UASC_UABINARY);

    private OpcUaHelper() {}


    /**
     * Checks an OPC UA {@link org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode} and throws meaningfull
     * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException} if status indicates an
     * error.
     *
     * @param statusCode the OPC UA status code received
     * @param errorMessage the message to use as prefix in the
     *            {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException}
     * @throws AssetConnectionException if {@code statusCode} indicates an error
     */
    public static void checkStatusCode(StatusCode statusCode, String errorMessage) throws AssetConnectionException {
        String message = errorMessage;
        if (statusCode.isBad()) {
            Optional<String[]> errorCodeDetails = StatusCodes.lookup(statusCode.getValue());
            if (errorCodeDetails.isPresent()) {
                if (errorCodeDetails.get().length >= 1) {
                    message += " - " + errorCodeDetails.get()[0];
                }
                if (errorCodeDetails.get().length > 1) {
                    message += " (details: " + errorCodeDetails.get()[1] + ")";
                }
            }
            throw new AssetConnectionException(message);
        }
    }


    /**
     * Parses a {@code nodeId}.
     *
     * @param client the underlying OPC UA client
     * @param nodeId the string representation of the nodeId
     * @return parsed nodeId
     * @throws IllegalArgumentException if parsing fails
     */
    public static NodeId parseNodeId(OpcUaClient client, String nodeId) {
        try {
            return ExpandedNodeId.parse(nodeId).toNodeIdOrThrow(client.getNamespaceTable());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }


    /**
     * Reads a value via OPC UA.
     *
     * @param client the OPC UA client to use
     * @param nodeId string representation of the node to read
     * @return the value of given node
     * @throws UaException if reading fails
     * @throws InterruptedException if reading fails
     * @throws ExecutionException if reading fails
     */
    public static DataValue readValue(OpcUaClient client, String nodeId) throws UaException, InterruptedException, ExecutionException {
        return client.readValue(0,
                TimestampsToReturn.Neither,
                client.getAddressSpace().getVariableNode(OpcUaHelper.parseNodeId(client, nodeId))
                        .getNodeId())
                .get();
    }


    /**
     * Writes a value via OPC UA.
     *
     * @param client the OPC UA client to use
     * @param nodeId string representation of the node to write to
     * @param value the value to write
     * @return the status code
     * @throws UaException if parsing node fails
     * @throws InterruptedException if writing fails
     * @throws ExecutionException if writing fails
     */
    public static StatusCode writeValue(OpcUaClient client, String nodeId, Object value) throws UaException, InterruptedException, ExecutionException {
        return client.writeValue(
                client.getAddressSpace().getVariableNode(OpcUaHelper.parseNodeId(client, nodeId))
                        .getNodeId(),
                new DataValue(new Variant(value)))
                .get();
    }


    /**
     * Connect to a OPC UA server. This method already respects all configuration properties like credentials and
     * numbers of retries.
     *
     * @param config the configuration to use
     * @param clientModifier optional way to modify client before connecting, e.g. for adding listeners
     * @return new OPC UA client instance that is already connected to the server
     * @throws AssetConnectionException if connecting fails
     * @throws ConfigurationInitializationException if configuration is invalid
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config, Consumer<OpcUaClient> clientModifier)
            throws AssetConnectionException, ConfigurationInitializationException {
        OpcUaClient client = createClient(config);
        if (Objects.nonNull(clientModifier)) {
            clientModifier.accept(client);
        }
        return connect(client);
    }


    /**
     * Connect to a OPC UA server. This method already respects all configuration properties.
     *
     * @param config the configuration to use
     * @return new OPC UA client instance that is already connected to the server
     * @throws AssetConnectionException if connecting fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if configuration is
     *             invalid
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config) throws AssetConnectionException, ConfigurationInitializationException {
        return connect(createClient(config));
    }


    private static IdentityProvider getIdentityProvider(OpcUaAssetConnectionConfig config) throws ConfigurationInitializationException {
        IdentityProvider retval;
        switch (config.getUserTokenType()) {
            case Certificate:
                retval = getIdentityProviderCertificate(config);
                break;

            case UserName:
                if (!StringHelper.isBlank(config.getUsername())) {
                    retval = new UsernameProvider(config.getUsername(), config.getPassword());
                }
                else {
                    throw new ConfigurationInitializationException("no user name specified!");
                }
                break;

            case Anonymous:
                retval = AnonymousProvider.INSTANCE;
                break;

            default:
                throw new ConfigurationInitializationException(String.format("UserTokenType %s not supported", config.getUserTokenType().toString()));
        }

        return retval;
    }


    private static IdentityProvider getIdentityProviderCertificate(OpcUaAssetConnectionConfig config)
            throws ConfigurationInitializationException {
        IdentityProvider retval;
        if (Objects.nonNull(config.getAuthenticationCertificate()) && Objects.nonNull(config.getAuthenticationCertificate().getKeyStorePath())) {
            File authenticationCertificateFile = new File(config.getAuthenticationCertificate().getKeyStorePath());
            if (!authenticationCertificateFile.exists()) {
                authenticationCertificateFile = config.getSecurityBaseDir().resolve(authenticationCertificateFile.toPath()).toFile();
            }
            if (authenticationCertificateFile.exists()) {
                try {
                    CertificateData certificateData = KeyStoreHelper.loadOrCreateCertificateData(
                            CertificateConfig.builder()
                                    .keyStoreType(config.getAuthenticationCertificate().getKeyStoreType())
                                    .keyStorePath(authenticationCertificateFile)
                                    .keyStorePassword(config.getAuthenticationCertificate().getKeyStorePassword())
                                    .keyPassword(config.getAuthenticationCertificate().getKeyPassword())
                                    .build(),
                            OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO);
                    retval = new X509IdentityProvider(certificateData.getCertificate(), certificateData.getKeyPair().getPrivate());
                }
                catch (IOException | GeneralSecurityException e) {
                    throw new ConfigurationInitializationException(String.format(
                            "error loading OPC UA client authentication certificate file (file: %s)",
                            config.getAuthenticationCertificate().getKeyStorePath()),
                            e);
                }
            }
            else {
                throw new ConfigurationInitializationException(String.format(
                        "OPC UA client authentication certificate file not found (file: %s)",
                        config.getAuthenticationCertificate().getKeyStorePath()));
            }
        }
        else {
            throw new ConfigurationInitializationException("no authentication certificate specified!");
        }
        return retval;
    }


    /**
     * Extracts transport profile from host URL is possible, otherwise throws IllegalArgumentException.
     *
     * @param host the host URL, e.g. https://example.com
     * @return the transport profile
     * @throws IllegalArgumentException if transport profile could not be determined
     */
    public static TransportProfile detectTransportProfile(String host) {
        Ensure.requireNonNull(host, "host must be non-null");
        return SUPPORTED_TRANSPORT_SCHEMES.stream()
                .filter(x -> host.startsWith(x.getScheme()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format(
                        "unsupported transport protocol scheme (host: %s, supported schemes: %s)",
                        host,
                        SUPPORTED_TRANSPORT_SCHEMES.stream().map(x -> x.getScheme()).collect(Collectors.joining(", ")))));
    }


    private static Optional<CertificateData> loadCertificate(File file, String keyStoreType, String keyAlias, String keyPassword, String keyStorePassword) {
        try {
            return Optional.of(KeyStoreHelper.loadCertificateData(file, keyStoreType, keyAlias, keyPassword, keyStorePassword));
        }
        catch (IOException | GeneralSecurityException e) {
            return Optional.empty();
        }
    }


    /**
     * Loads the authentication certificate.
     *
     * @param securityBaseDir the security base dir
     * @param certificateFile the keystore file
     * @param keyStoreType type of the keyStore
     * @param keyAlias key alias to use
     * @param keyPassword key password to use
     * @param keyStorePassword keyStore password to use
     * @return the certificate data
     * @throws ConfigurationInitializationException if loading fails
     */
    public static CertificateData loadAuthenticationCertificate(
                                                                Path securityBaseDir,
                                                                File certificateFile,
                                                                String keyStoreType,
                                                                String keyAlias,
                                                                String keyPassword,
                                                                String keyStorePassword)
            throws ConfigurationInitializationException {
        return loadCertificate("authentication",
                securityBaseDir,
                certificateFile,
                keyStoreType,
                keyAlias,
                keyPassword,
                keyStorePassword);
    }


    /**
     * Loads the application certificate.
     *
     * @param securityBaseDir the security base dir
     * @param certificateFile the keystore file
     * @param keyStoreType type of the keyStore
     * @param keyAlias key alias to use
     * @param keyPassword key password to use
     * @param keyStorePassword keyStore password to use
     * @return the certificate data
     * @throws ConfigurationInitializationException if loading fails
     */
    public static CertificateData loadApplicationCertificate(Path securityBaseDir,
                                                             File certificateFile,
                                                             String keyStoreType,
                                                             String keyAlias,
                                                             String keyPassword,
                                                             String keyStorePassword)
            throws ConfigurationInitializationException {
        return loadCertificate("application", securityBaseDir, certificateFile, keyStoreType, keyAlias, keyPassword, keyStorePassword);
    }


    private static CertificateData loadCertificate(
                                                   String name,
                                                   Path securityBaseDir,
                                                   File certificateFile,
                                                   String keyStoreType,
                                                   String keyAlias,
                                                   String keyPassword,
                                                   String keyStorePassword)
            throws ConfigurationInitializationException {
        Optional<CertificateData> result;
        // try loading from given path (either absolute or relative)
        result = loadCertificate(certificateFile, keyStoreType, keyAlias, keyPassword, keyStorePassword);
        if (result.isPresent()) {
            LOGGER.debug("Using OPC UA client {} certificate from {}", name, certificateFile);
        }
        else {
            // try loading relative to securityBaseDir
            result = loadCertificate(
                    securityBaseDir.resolve(certificateFile.toPath()).toFile(), keyStoreType, keyAlias, keyPassword, keyStorePassword);
            if (result.isPresent()) {
                LOGGER.debug("Using OPC UA client {} certificate from {}", name, securityBaseDir.resolve(certificateFile.toPath()));
            }
            else {
                try {
                    // if still empty, generate
                    result = Optional.of(KeyStoreHelper.generateSelfSigned(OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO));
                    // save generated certificate
                    File newFile;
                    if (certificateFile.isAbsolute()) {
                        newFile = certificateFile;
                    }
                    else {
                        newFile = securityBaseDir.resolve(certificateFile.toPath()).toFile();
                    }
                    KeyStoreHelper.save(
                            result.get(),
                            newFile,
                            keyStoreType,
                            keyAlias,
                            keyPassword,
                            keyStorePassword);
                }
                catch (IOException | GeneralSecurityException e) {
                    throw new ConfigurationInitializationException(String.format("error generating OPC UA client %s certificate", name), e);
                }
                LOGGER.debug("Generating new OPC UA client {} certificate", name);
            }
        }
        return result.orElseThrow(() -> new ConfigurationInitializationException(String.format(
                "unable to load or generate OPC UA client %s certificate", name)));
    }


    private static OpcUaClient createClient(OpcUaAssetConnectionConfig config)
            throws AssetConnectionException, ConfigurationInitializationException {
        CertificateData applicationCertificate = loadApplicationCertificate(
                config.getSecurityBaseDir(),
                new File(config.getApplicationCertificate().getKeyStorePath()),
                config.getApplicationCertificate().getKeyStoreType(),
                config.getApplicationCertificate().getKeyAlias(),
                config.getApplicationCertificate().getKeyPassword(),
                config.getApplicationCertificate().getKeyStorePassword());

        ClientCertificateValidator certificateValidator;
        try {
            Files.createDirectories(config.getSecurityBaseDir());
            certificateValidator = new DefaultClientCertificateValidator(new DefaultTrustListManager(SecurityPathHelper.pki(config.getSecurityBaseDir()).toFile()));
        }
        catch (IOException e) {
            throw new ConfigurationInitializationException("unable to initialize OPC UA client security", e);
        }

        IdentityProvider identityProvider = getIdentityProvider(config);
        try {
            return OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> e.getSecurityPolicyUri().equals(config.getSecurityPolicy().getUri()))
                            .filter(e -> e.getSecurityMode() == config.getSecurityMode())
                            .filter(e -> Objects.equals(config.getTransportProfile().getUri(), e.getTransportProfileUri()))
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english(OpcUaConstants.CERTIFICATE_APPLICATION_NAME))
                            .setApplicationUri(CertificateUtil.getSanUri(applicationCertificate.getCertificate())
                                    .orElse(OpcUaConstants.CERTIFICATE_APPLICATION_URI))
                            //.setProductUri("urn:de:fraunhofer:iosb:ilt:faast:asset-connection")
                            .setIdentityProvider(identityProvider)
                            .setRequestTimeout(uint(config.getRequestTimeout()))
                            .setAcknowledgeTimeout(uint(config.getAcknowledgeTimeout()))
                            .setKeyPair(applicationCertificate.getKeyPair())
                            .setCertificate(applicationCertificate.getCertificate())
                            .setCertificateChain(applicationCertificate.getCertificateChain())
                            .setCertificateValidator(certificateValidator)
                            .build());
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("error creating OPC UA client (host: %s)", config.getHost()), e);
        }
    }


    private static OpcUaClient connect(OpcUaClient client) throws AssetConnectionException {
        try {
            client.connect().get();
        }
        catch (InterruptedException | ExecutionException e) {
            if (e instanceof UaServiceFaultException) {
                checkUserAuthenticationError((UaServiceFaultException) e, client.getConfig().getEndpoint().getEndpointUrl());
            }
            else if (e.getCause() instanceof UaServiceFaultException) {
                checkUserAuthenticationError((UaServiceFaultException) e.getCause(), client.getConfig().getEndpoint().getEndpointUrl());
            }
            throw new AssetConnectionException(String.format(
                    "error opening OPC UA connection (host: %s)",
                    client.getConfig().getEndpoint().getEndpointUrl()),
                    e);
        }
        return client;
    }


    private static void checkUserAuthenticationError(UaServiceFaultException exception, String endpointUrl) {
        if ((exception.getStatusCode().getValue() == StatusCodes.Bad_IdentityTokenRejected)
                || (exception.getStatusCode().getValue() == StatusCodes.Bad_IdentityTokenInvalid)) {
            throw new IllegalArgumentException(String.format("Identity Token invalid (host: %s)", endpointUrl));
        }
        if ((exception.getStatusCode().getValue() == StatusCodes.Bad_UserAccessDenied)
                || (exception.getStatusCode().getValue() == StatusCodes.Bad_IdentityTokenInvalid)) {
            throw new IllegalArgumentException(String.format("Access Denied (host: %s)", endpointUrl));
        }
    }
}
