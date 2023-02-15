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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.security.KeyStoreLoader;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.client.security.ClientCertificateValidator;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for working with OPC UA connections.
 */
public class OpcUaHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaHelper.class);

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


    private static OpcUaClient createClient(OpcUaAssetConnectionConfig config) throws AssetConnectionException, ConfigurationInitializationException {
        String securityBaseDir = config.getSecurityBaseDir();

        Path securityDir = Paths.get(securityBaseDir, "client", "security");
        KeyStoreLoader keyStoreLoader;
        ClientCertificateValidator certificateValidator;
        try {
            Files.createDirectories(securityDir);
            File pkiDir = securityDir.resolve("pki").toFile();
            LOGGER.trace("security dir: {}", securityDir.toAbsolutePath());
            LOGGER.trace("security pki dir: {}", pkiDir.getAbsolutePath());

            keyStoreLoader = new KeyStoreLoader().load(securityDir);
            certificateValidator = new DefaultClientCertificateValidator(new DefaultTrustListManager(pkiDir));
        }
        catch (IOException e) {
            throw new ConfigurationInitializationException("unable to initialize OPC UA client security", e);
        }

        IdentityProvider identityProvider = StringHelper.isBlank(config.getUsername())
                ? AnonymousProvider.INSTANCE
                : new UsernameProvider(config.getUsername(), config.getPassword());
        OpcUaClient client;
        try {
            client = OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> e.getSecurityPolicyUri().equals(config.getSecurityPolicy().getUri()))
                            .filter(e -> e.getSecurityMode() == config.getSecurityMode())
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english(OpcUaConstants.APPLICATION_NAME))
                            .setApplicationUri(OpcUaConstants.APPLICATION_URI)
                            // TODO Is this needed? Why?
                            //.setProductUri("urn:de:fraunhofer:iosb:ilt:faast:asset-connection")
                            .setIdentityProvider(identityProvider)
                            .setRequestTimeout(uint(config.getRequestTimeout()))
                            .setAcknowledgeTimeout(uint(config.getAcknowledgeTimeout()))
                            .setKeyPair(keyStoreLoader.getClientKeyPair())
                            .setCertificate(keyStoreLoader.getClientCertificate())
                            .setCertificateChain(keyStoreLoader.getClientCertificateChain())
                            .setCertificateValidator(certificateValidator)
                            .build());
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("error creating OPC UA client (host: %s)", config.getHost()), e);
        }
        return client;
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
