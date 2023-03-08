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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.LoggerFactory;


/**
 * Utility class for working with OPC UA connections.
 */
public class OpcUaHelper {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OpcUaHelper.class);
    public static final String NODE_ID_SEPARATOR = ";";
    public static final String APPLICATION_URI = "urn:de:fraunhofer:iosb:ilt:faaast:service:assetconnection:opcua";
    public static final String APPLICATION_NAME = "FA³ST Asset Connection";

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
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config, Consumer<OpcUaClient> clientModifier) throws AssetConnectionException {
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
     */
    public static OpcUaClient connect(OpcUaAssetConnectionConfig config) throws AssetConnectionException {
        return connect(createClient(config));
    }


    private static OpcUaClient createClient(OpcUaAssetConnectionConfig config) throws AssetConnectionException {
        IdentityProvider identityProvider = StringUtils.isAllBlank(config.getUsername())
                ? AnonymousProvider.INSTANCE
                : new UsernameProvider(config.getUsername(), config.getPassword());
        OpcUaClient client;
        try {
            client = OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english(APPLICATION_NAME))
                            .setApplicationUri(APPLICATION_URI)
                            .setIdentityProvider(identityProvider)
                            .setRequestTimeout(uint(config.getRequestTimeout()))
                            .setAcknowledgeTimeout(uint(config.getAcknowledgeTimeout()))
                            .build());
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("error creating OPC UA client (host: %s)", config.getHost()), e);
        }
        return client;
    }


    private static OpcUaClient connect(OpcUaClient client) throws AssetConnectionException {
        try {
            client.connect().get();
        }
        catch (InterruptedException | ExecutionException e) {
            throw new AssetConnectionException(String.format(
                    "error opening OPC UA connection (host: %s)",
                    client.getConfig().getEndpoint().getEndpointUrl()),
                    e);
        }
        return client;
    }
}
