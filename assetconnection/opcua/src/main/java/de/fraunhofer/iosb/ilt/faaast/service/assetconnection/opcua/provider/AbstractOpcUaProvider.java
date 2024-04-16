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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.AbstractOpcUaProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.core.nodes.Node;
import org.eclipse.milo.opcua.stack.core.UaException;


/**
 * Superclass for all OPC UA provider classes.
 *
 * @param <T> type of the asset provider config
 */
public abstract class AbstractOpcUaProvider<T extends AbstractOpcUaProviderConfig> {

    protected final ServiceContext serviceContext;
    protected final Reference reference;
    protected final T providerConfig;
    protected final ValueConverter valueConverter;
    protected OpcUaClient client;
    protected Node node;

    protected AbstractOpcUaProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            T providerConfig,
            ValueConverter valueConverter) throws InvalidConfigurationException, AssetConnectionException {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        Ensure.requireNonNull(valueConverter, "valueConverter must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.providerConfig = providerConfig;
        this.valueConverter = valueConverter;
        validateNode();
    }


    public T getProviderConfig() {
        return providerConfig;
    }


    private void validateNode() throws InvalidConfigurationException, AssetConnectionException {
        String baseErrorMsg = "invalid OPC UA provider configuration";
        try {
            node = client.getAddressSpace().getNode(OpcUaHelper.parseNodeId(client, providerConfig.getNodeId()));
            Ensure.requireNonNull(node, new AssetConnectionException(
                    String.format("%s - unable to access node (nodeId: %s)",
                            baseErrorMsg,
                            providerConfig.getNodeId())));
        }
        catch (IllegalArgumentException | UaException e) {
            throw new InvalidConfigurationException(
                    String.format("%s - could not parse nodeId (nodeId: %s)",
                            baseErrorMsg,
                            providerConfig.getNodeId()),
                    e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, client, reference, providerConfig, valueConverter, node);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractOpcUaProvider)) {
            return false;
        }
        final AbstractOpcUaProvider<?> that = (AbstractOpcUaProvider<?>) obj;
        return Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(client, that.client)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(valueConverter, that.valueConverter)
                && Objects.equals(node, that.node);
    }

}
