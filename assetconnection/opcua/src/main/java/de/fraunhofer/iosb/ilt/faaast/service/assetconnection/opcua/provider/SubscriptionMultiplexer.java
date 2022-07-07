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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConversionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps multiple FA³ST subscriptions to a single OPC UA subscription
 */
public class SubscriptionMultiplexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionMultiplexer.class);
    private final ServiceContext serviceContext;
    private final OpcUaSubscriptionProviderConfig providerConfig;
    private final Reference reference;
    private final OpcUaClient client;
    private final Set<NewDataListener> listeners;
    private final ValueConverter valueConverter;
    private final ManagedSubscription opcUaSubscription;
    private ManagedDataItem dataItem;
    private Datatype datatype;

    public SubscriptionMultiplexer(ServiceContext serviceContext,
            Reference reference,
            OpcUaSubscriptionProviderConfig providerConfig,
            OpcUaClient client,
            ManagedSubscription opcUaSubscription,
            ValueConverter valueConverter) throws AssetConnectionException {
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(providerConfig, "providerConfig must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(opcUaSubscription, "opcUaSubscription must be non-null");
        Ensure.requireNonNull(valueConverter, "valueConverter must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.providerConfig = providerConfig;
        this.client = client;
        this.opcUaSubscription = opcUaSubscription;
        this.valueConverter = valueConverter;
        this.listeners = new HashSet<>();
        init();
    }


    private void init() throws AssetConnectionException {
        TypeInfo<?> typeInfo = serviceContext.getTypeInfo(reference);
        if (typeInfo == null) {
            throw new AssetConnectionException(
                    String.format("Could not resolve type information (reference: %s)",
                            AasUtils.asString(reference)));
        }
        if (!ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new AssetConnectionException(
                    String.format("Reference must point to element with value (reference: %s)",
                            AasUtils.asString(reference)));
        }
        ElementValueTypeInfo valueTypeInfo = (ElementValueTypeInfo) typeInfo;
        if (!PropertyValue.class.isAssignableFrom(valueTypeInfo.getType())) {
            throw new AssetConnectionException(String.format("Unsupported element type (reference: %s, element type: %s)",
                    AasUtils.asString(reference),
                    valueTypeInfo.getType()));
        }
        datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("Missing datatype (reference: %s)",
                    AasUtils.asString(reference)));
        }
        try {
            dataItem = opcUaSubscription.createDataItem(
                    OpcUaHelper.parseNodeId(client, providerConfig.getNodeId()),
                    LambdaExceptionHelper.rethrowConsumer(
                            x -> x.addDataValueListener(LambdaExceptionHelper.rethrowConsumer(this::notify))));
        }
        catch (UaException e) {
            LOGGER.warn("Could not create subscrption item (reference: {}, nodeId: {})",
                    AasUtils.asString(reference),
                    providerConfig.getNodeId(),
                    e);
        }
    }


    private void notify(DataValue value) {
        try {
            DataElementValue newValue = new PropertyValue(valueConverter.convert(value.getValue(), datatype));
            listeners.forEach(x -> {
                try {
                    x.newDataReceived(newValue);
                }
                catch (Exception e) {
                    LOGGER.warn("Unexpected exception while invoking newDataReceived handler", e);
                }
            });
        }
        catch (ValueConversionException e) {
            LOGGER.warn("received illegal value via OPC UA subscription - type conversion faild (value: {}, target type: {}, nodeId: {})",
                    value.getValue(),
                    datatype,
                    providerConfig.getNodeId(),
                    e);
        }
    }


    /**
     * Adds a listener
     *
     * @param listener The listener to add
     */
    public void addListener(NewDataListener listener) {
        listeners.add(listener);
    }


    /**
     * Checks if the multiplexer is active, i.e. there is an active OPC UA
     * subscription that has not been closed.
     *
     * @return true if active, otherwise false
     */
    public boolean isActive() {
        return dataItem != null;
    }


    /**
     * Removes a listener. If the last listener is removed, the OPC UA
     * subscription is closed and the multiplexer becomes inactive
     *
     * @param listener The listener to remove
     * @throws AssetConnectionException if closing the OPC UA subscription fails
     */
    public void removeListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            close();
            dataItem = null;
        }
    }


    /**
     * Closes the multiplexer, i.e. ends the underlying OPC UA subscription
     *
     * @throws AssetConnectionException if closing the OPC UA subscription fails
     */
    public void close() throws AssetConnectionException {
        try {
            dataItem.delete();
        }
        catch (UaException e) {
            throw new AssetConnectionException(
                    String.format("Removing subscription failed (reference: %s, nodeId: %s)",
                            AasUtils.asString(reference),
                            providerConfig.getNodeId()),
                    e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(serviceContext, client, reference, providerConfig, valueConverter, listeners, opcUaSubscription, dataItem, datatype);
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
        final SubscriptionMultiplexer that = (SubscriptionMultiplexer) obj;
        return Objects.equals(serviceContext, that.serviceContext)
                && Objects.equals(client, that.client)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig)
                && Objects.equals(valueConverter, that.valueConverter)
                && Objects.equals(listeners, that.listeners)
                && Objects.equals(opcUaSubscription, that.opcUaSubscription)
                && Objects.equals(dataItem, that.dataItem)
                && Objects.equals(datatype, that.datatype);
    }
}
