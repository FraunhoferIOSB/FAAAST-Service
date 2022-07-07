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
import java.util.Objects;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;


/**
 * Implemenation of SubscriptionProvider for OPC UA asset connections. Supports
 * subscribing to OPC UA.
 */
public class OpcUaSubscriptionProvider extends AbstractOpcUaProvider<OpcUaSubscriptionProviderConfig> implements AssetSubscriptionProvider {

    private final ManagedSubscription opcUaSubscription;
    private final Map<String, SubscriptionMultiplexer> subscriptions;

    public OpcUaSubscriptionProvider(ServiceContext serviceContext,
            Reference reference,
            OpcUaSubscriptionProviderConfig providerConfig,
            OpcUaClient client,
            ManagedSubscription opcUaSubscription,
            ValueConverter valueConverter) {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        Ensure.requireNonNull(opcUaSubscription, "opcUaSubscription must be non-null");
        this.opcUaSubscription = opcUaSubscription;
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
        subscriptions.values().forEach(LambdaExceptionHelper.rethrowConsumer(SubscriptionMultiplexer::close));
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


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), opcUaSubscription, subscriptions);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpcUaSubscriptionProvider that = (OpcUaSubscriptionProvider) obj;
        return super.equals(that)
                && Objects.equals(opcUaSubscription, that.opcUaSubscription)
                && Objects.equals(subscriptions, that.subscriptions);
    }

}
