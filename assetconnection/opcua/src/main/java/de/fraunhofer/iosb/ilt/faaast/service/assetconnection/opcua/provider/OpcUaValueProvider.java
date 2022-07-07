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
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.util.Objects;
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
public class OpcUaValueProvider extends AbstractOpcUaProvider<OpcUaValueProviderConfig> implements AssetValueProvider {

    private VariableNode node;
    private Datatype datatype;

    public OpcUaValueProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaValueProviderConfig providerConfig,
            ValueConverter valueConverter) throws AssetConnectionException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
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


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), node, datatype);
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
        final OpcUaValueProvider that = (OpcUaValueProvider) obj;
        return super.equals(that)
                && Objects.equals(node, that.node)
                && Objects.equals(datatype, that.datatype);
    }
}
