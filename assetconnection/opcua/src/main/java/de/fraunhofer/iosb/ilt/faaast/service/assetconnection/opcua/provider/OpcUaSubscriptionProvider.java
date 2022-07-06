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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;


/**
 * Implemenation of SubscriptionProvider for OPC UA asset connections. Supports
 * subscribing to OPC UA.
 */
public class OpcUaSubscriptionProvider implements AssetSubscriptionProvider {

    private final ServiceContext serviceContext;
    private final OpcUaClient client;
    private final Reference reference;
    private final ValueConverter valueConverter;
    private final OpcUaSubscriptionProviderConfig providerConfig;
    private final ManagedSubscription opcUaSubscription;
    private final Map<String, SubscriptionMultiplexer> subscriptions;
    private VariableNode node;

    /**
     * Creates new instance.
     *
     * @param serviceContext the service context
     * @param client OPC UA client to use
     * @param reference reference to the AAS element
     * @param providerConfig configuration
     * @param opcUaSubscription existing OPC UA subscription object to use
     * @param valueConverter value converter to use
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException
     *             if initialization fails
     * @throws IllegalArgumentException if serviceContext is null
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if providerConfig is null
     * @throws IllegalArgumentException if valueProvider is null
     */
    public OpcUaSubscriptionProvider(ServiceContext serviceContext,
            Reference reference,
            OpcUaSubscriptionProviderConfig providerConfig,
            OpcUaClient client,
            ManagedSubscription opcUaSubscription,
            ValueConverter valueConverter) throws AssetConnectionException {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(opcUaSubscription, "opcUaSubscription must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        Ensure.requireNonNull(valueConverter, "valueConverter must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.opcUaSubscription = opcUaSubscription;
        this.providerConfig = providerConfig;
        this.valueConverter = valueConverter;
        this.subscriptions = new HashMap<>();
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (!subscriptions.containsKey(providerConfig.getNodeId())) {
            subscriptions.put(providerConfig.getNodeId(), new SubscriptionMultiplexer(
                    serviceContext,
                    reference,
                    providerConfig,
                    client,
                    opcUaSubscription,
                    valueConverter));
        }
        subscriptions.get(providerConfig.getNodeId()).addListener(listener);
    }


    /**
     * Ends all OPC UA subscriptions
     *
     * @throws AssetConnectionException if unsubscribing via OPC UA fails
     */
    public void close() throws AssetConnectionException {
        subscriptions.values().forEach(LambdaExceptionHelper.rethrowConsumer(x -> x.close()));
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        if (subscriptions.containsKey(providerConfig.getNodeId())) {
            try {
                subscriptions.get(providerConfig.getNodeId()).removeListener(listener);
                if (!subscriptions.get(providerConfig.getNodeId()).isActive()) {
                    subscriptions.remove(providerConfig.getNodeId());
                }
            }
            catch (AssetConnectionException e) {
                throw new AssetConnectionException(
                        String.format("Removing subscription failed (reference: %s, nodeId: %s)",
                                AasUtils.asString(reference),
                                providerConfig.getNodeId()),
                        e);
            }
        }
    }

}
