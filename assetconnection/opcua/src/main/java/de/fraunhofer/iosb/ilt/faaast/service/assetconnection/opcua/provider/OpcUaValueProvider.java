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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.concurrent.ExecutionException;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;


/**
 * Implemenation of ValueProvider for OPC UA asset connections. Supports reading
 * and writing values from/to OPC UA.
 */
public class OpcUaValueProvider implements AssetValueProvider {

    private final ServiceContext serviceContext;
    private final OpcUaClient client;
    private final Reference reference;
    private final OpcUaValueProviderConfig providerConfig;
    private final ValueConverter valueConverter;
    private VariableNode node;
    private Datatype datatype;

    /**
     * Creates new instance.
     *
     * @param serviceContext the service context
     * @param client OPC UA client to use
     * @param reference reference to the AAS element
     * @param providerConfig configuration
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
    public OpcUaValueProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaValueProviderConfig providerConfig,
            ValueConverter valueConverter) throws AssetConnectionException {
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
        init();
    }


    private void init() throws AssetConnectionException {
        final String baseErrorMessage = "error registering value provider";
        TypeInfo<?> typeInfo = serviceContext.getTypeInfo(reference);
        if (typeInfo == null) {
            throw new AssetConnectionException(
                    String.format("%s - could not resolve type information (reference: %s)",
                            baseErrorMessage,
                            AasUtils.asString(reference)));
        }
        if (!ElementValueTypeInfo.class.isAssignableFrom(typeInfo.getClass())) {
            throw new AssetConnectionException(
                    String.format("%s - reference must point to element with value (reference: %s)",
                            baseErrorMessage,
                            AasUtils.asString(reference)));
        }
        ElementValueTypeInfo valueTypeInfo = (ElementValueTypeInfo) typeInfo;
        if (!PropertyValue.class.isAssignableFrom(valueTypeInfo.getType())) {
            throw new AssetConnectionException(String.format("%s - unsupported element type (reference: %s, element type: %s)",
                    baseErrorMessage,
                    AasUtils.asString(reference),
                    valueTypeInfo.getType()));
        }
        datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("%s - missing datatype (reference: %s)",
                    baseErrorMessage,
                    AasUtils.asString(reference)));
        }
        try {
            node = client.getAddressSpace().getVariableNode(OpcUaHelper.parseNodeId(client, providerConfig.getNodeId()));
        }
        catch (UaException e) {
            throw new AssetConnectionException(
                    String.format("%s - could not parse nodeId (nodeId: %s)",
                            baseErrorMessage,
                            providerConfig.getNodeId()),
                    e);
        }
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        try {
            DataValue dataValue = client.readValue(0, TimestampsToReturn.Neither, node.getNodeId()).get();
            OpcUaHelper.checkStatusCode(dataValue.getStatusCode(), "error reading value from asset conenction");
            return new PropertyValue(valueConverter.convert(dataValue.getValue(), datatype));
        }
        catch (AssetConnectionException | InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        if (value == null) {
            throw new AssetConnectionException(
                    String.format("error setting value on asset connection - value must be non-null (reference: %s)", AasUtils.asString(reference)));
        }
        if (!PropertyValue.class.isAssignableFrom(value.getClass())) {
            throw new AssetConnectionException(String.format("error setting value on asset connection - unsupported element type (reference: %s, element type: %s)",
                    AasUtils.asString(reference),
                    value.getClass()));
        }
        try {
            StatusCode result = client.writeValue(node.getNodeId(), new DataValue(
                    valueConverter.convert(((PropertyValue) value).getValue(), node.getDataType()),
                    null,
                    null)).get();
            OpcUaHelper.checkStatusCode(result, "error setting value on asset connection");
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException("error writing asset connection value", e);
        }
    }
}
