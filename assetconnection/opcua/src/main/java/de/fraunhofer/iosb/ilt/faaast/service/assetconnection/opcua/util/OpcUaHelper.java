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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.security.CertificateData;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.transport.TransportProfile;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for working with OPC UA connections.
 */
public class OpcUaHelper {

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
     * Connect to a OPC UA server. This method already respects all configuration properties like credentials and
     * numbers of retries.
     *
     * @param config the configuration to use
     * @param clientModifier optional way to modify client before connecting, e.g. for adding listeners
     * @return new OPC UA client instance that is already connected to the server
     * @throws AssetConnectionException if connecting fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if configuration is
     *             invalid
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config, Consumer<OpcUaClient> clientModifier)
            throws AssetConnectionException, ConfigurationInitializationException {
        OpcUaClient client = createClient(config);
        if (Objects.nonNull(clientModifier)) {
            clientModifier.accept(client);
        }
        return connect(client, config.getRetries());
    }


    /**
     * Connect to a OPC UA server.This method already respects all configuration properties like credentials and numbers
     * of retries.
     *
     * @param config the configuration to use
     * @return new OPC UA client instance that is already connected to the server
     * @throws AssetConnectionException if connecting fails
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if configuration is
     *             invalid
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config) throws AssetConnectionException, ConfigurationInitializationException {
        return connect(createClient(config), config.getRetries());
    }


    private static IdentityProvider getIdentityProvider(OpcUaAssetConnectionConfig config) throws ConfigurationInitializationException {
        // TODO depend on UserTokenType   
        if (Objects.nonNull(config.getAuthenticationCertificateFile())) {
            File authenticationCertificateFile = config.getAuthenticationCertificateFile();
            if (!authenticationCertificateFile.exists()) {
                authenticationCertificateFile = config.getSecurityBaseDir().resolve(authenticationCertificateFile.toPath()).toFile();
            }
            if (authenticationCertificateFile.exists()) {
                try {
                    CertificateData certificateData = KeystoreHelper.loadOrCreate(authenticationCertificateFile, config.getAuthenticationCertificatePassword(),
                            OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO);
                    return new X509IdentityProvider(certificateData.getCertificate(), certificateData.getKeyPair().getPrivate());
                }
                catch (IOException | GeneralSecurityException e) {
                    throw new ConfigurationInitializationException(String.format(
                            "error loading OPC UA client authentication certificate file (file: %s)",
                            config.getAuthenticationCertificateFile()),
                            e);
                }
            }
        }
        if (!StringHelper.isBlank(config.getUsername())) {
            return new UsernameProvider(config.getUsername(), config.getPassword());
        }
        return AnonymousProvider.INSTANCE;
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


    private static EndpointDescription findBestMatchingEndpoint(OpcUaAssetConnectionConfig config) throws AssetConnectionException {
        try {
            return DiscoveryClient.getEndpoints(config.getHost()).get().stream()
                    .filter(e -> e.getSecurityPolicyUri().equals(config.getSecurityPolicy().getUri()))
                    .filter(e -> e.getSecurityMode() == config.getSecurityMode())
                    .filter(e -> Objects.equals(config.getTransportProfile().getUri(), e.getTransportProfileUri()))
                    .findFirst()
                    .orElseThrow(() -> new AssetConnectionException(
                            String.format(
                                    "No matching endpoint found (host: %s, security policy: %s, security mode: %s)",
                                    config.getHost(),
                                    config.getSecurityPolicy(),
                                    config.getSecurityMode())));
        }
        catch (InterruptedException | ExecutionException e) {
            throw new AssetConnectionException(String.format(
                    "Unable to fetch available endpoints (host: %s)",
                    config.getHost(),
                    config.getSecurityPolicy(),
                    config.getSecurityMode()), e);
        }
    }


    private static Optional<CertificateData> loadCertificate(File file, String password) {
        try {
            return Optional.of(KeystoreHelper.load(file, password));
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
     * @param certificatePassword the password for the keystore
     * @return the certificate data
     * @throws ConfigurationInitializationException if loading fails
     */
    public static CertificateData loadAuthenticationCertificate(Path securityBaseDir, File certificateFile, String certificatePassword)
            throws ConfigurationInitializationException {
        return loadCertificate("authentication", securityBaseDir, certificateFile, certificatePassword);
    }


    /**
     * Loads the application certificate.
     *
     * @param securityBaseDir the security base dir
     * @param certificateFile the keystore file
     * @param certificatePassword the password for the keystore
     * @return the certificate data
     * @throws ConfigurationInitializationException if loading fails
     */
    public static CertificateData loadApplicationCertificate(Path securityBaseDir, File certificateFile, String certificatePassword) throws ConfigurationInitializationException {
        return loadCertificate("application", securityBaseDir, certificateFile, certificatePassword);
    }


    private static CertificateData loadCertificate(String name, Path securityBaseDir, File certificateFile, String certificatePassword)
            throws ConfigurationInitializationException {
        Optional<CertificateData> result;
        // try loading from given path (either absolute or relative)
        result = loadCertificate(certificateFile, certificatePassword);
        if (result.isPresent()) {
            LOGGER.debug("Using OPC UA client {} certificate from {}", name, certificateFile);
        }
        else {
            // try loading relative to securityBaseDir
            result = loadCertificate(
                    securityBaseDir.resolve(certificateFile.toPath()).toFile(), certificatePassword);
            if (result.isPresent()) {
                LOGGER.debug("Using OPC UA client {} certificate from {}", name, securityBaseDir.resolve(certificateFile.toPath()));
            }
            else {
                try {
                    // if still empty, generate
                    result = Optional.of(KeystoreHelper.generateSelfSigned(OpcUaConstants.DEFAULT_APPLICATION_CERTIFICATE_INFO));
                }
                catch (KeyStoreException | NoSuchAlgorithmException e) {
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
                config.getApplicationCertificateFile(),
                config.getApplicationCertificatePassword());

        ClientCertificateValidator certificateValidator;
        try {
            Files.createDirectories(config.getSecurityBaseDir());
            certificateValidator = new DefaultClientCertificateValidator(new DefaultTrustListManager(SecurityPathHelper.pki(config.getSecurityBaseDir()).toFile()));
        }
        catch (IOException e) {
            throw new ConfigurationInitializationException("unable to initialize OPC UA client security", e);
        }
        EndpointDescription endpoint = findBestMatchingEndpoint(config);
        LOGGER.debug("using endpoint: {}", endpoint);

        IdentityProvider identityProvider = getIdentityProvider(config);
        try {
            return OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> Objects.equals(endpoint, e))
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english(OpcUaConstants.CERTIFICATE_APPLICATION_NAME))
                            .setApplicationUri(CertificateUtil.getSanUri(applicationCertificate.getCertificate())
                                    .orElse(OpcUaConstants.CERTIFICATE_APPLICATION_URI))
                            //                            .setProductUri("urn:de:fraunhofer:iosb:ilt:faast:asset-connection")
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


    private static OpcUaClient connect(OpcUaClient client, int retries) throws AssetConnectionException {
        boolean success = false;
        int count = 0;
        do {
            try {
                client.connect().get();
                success = true;
            }
            catch (InterruptedException | ExecutionException e) {
                // ignore
                e.printStackTrace();
                if (count >= retries) {
                    throw new AssetConnectionException(String.format(
                            "error opening OPC UA connection (host: %s)",
                            client.getConfig().getEndpoint().getEndpointUrl()),
                            e);
                }
                else {
                    LOGGER.debug("Opening OPC UA connection failed on try {}/{} (host: {})",
                            count + 1,
                            retries + 1,
                            client.getConfig().getEndpoint().getEndpointUrl());
                    if (InterruptedException.class.isAssignableFrom(e.getClass())) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            finally {
                count++;
            }
        } while (!success && count <= retries);
        return client;
    }

}
