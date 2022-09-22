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
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;


public class OpcUaHelper {

    public static final String NODE_ID_SEPARATOR = ";";

    private OpcUaHelper() {}


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


    public static NodeId parseNodeId(OpcUaClient client, String nodeId) {
        try {
            return ExpandedNodeId.parse(nodeId).toNodeIdOrThrow(client.getNamespaceTable());
        }
        catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }


    public static DataValue readValue(OpcUaClient client, String nodeId) throws UaException, InterruptedException, ExecutionException {
        return client.readValue(0,
                TimestampsToReturn.Neither,
                client.getAddressSpace().getVariableNode(OpcUaHelper.parseNodeId(client, nodeId))
                        .getNodeId())
                .get();
    }


    public static OpcUaClient createClient(URI opcUrl, IdentityProvider identityProvider, String applicationName) throws UaException {
        return OpcUaClient.create(
                opcUrl.toString(),
                endpoints -> Optional.of(
                        EndpointUtil.updateUrl(
                                endpoints.stream()
                                        .findFirst()
                                        .get(),
                                opcUrl.getHost())),
                configBuilder -> configBuilder
                        .setApplicationName(LocalizedText.english(applicationName))
                        .setApplicationUri("urn:de:fraunhofer:iosb:ilt:faaast" + UUID.randomUUID())
                        .setIdentityProvider(identityProvider)
                        .setRequestTimeout(uint(60000))
                        .build());
    }


    public static OpcUaClient createClient(String opcUrl, IdentityProvider identityProvider) throws UaException {
        return createClient(URI.create(opcUrl), identityProvider);
    }


    public static OpcUaClient createClient(URI opcUrl, IdentityProvider identityProvider) throws UaException {
        return createClient(opcUrl, identityProvider, UUID.randomUUID().toString());
    }
}
