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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.*;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.content.ContentParserFactory;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.utils.OpcUaUtils;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.stream.Stream;


/**
 * Implementation of OPC UA Asset Connection
 */
public class OpcUaAssetConnection
        implements AssetConnection<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    private OpcUaAssetConnectionConfig config;
    private Map<Reference, AssetValueProvider> valueProviders;
    private Map<Reference, AssetOperationProvider> operationProviders;
    private Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private OpcUaClient client = null;
    private ServiceContext context;

    public OpcUaAssetConnection() {
        this.valueProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
    }

    @Override
    public void close() {

    }

    @Override
    public void init(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext context) throws AssetConnectionException {
        try {
            this.context = context;
            this.config = config;
            client = OpcUaUtils.createClient(config.getHost(), AnonymousProvider.INSTANCE);
            client.connect().get();
        }
        catch (UaException|InterruptedException|ExecutionException ex) {
            ex.printStackTrace();
        }

        config.getValueProviders().forEach((k, v) -> {
            try {
                registerValueProvider(k, v);
            } catch (AssetConnectionException e) {
                e.printStackTrace();
            }
        });
        config.getOperationProviders().forEach((k, v) -> {
            registerOperationProvider(k, v);
        });
        config.getSubscriptionProviders().forEach((k, v) -> {
            try {
                registerSubscriptionProvider(k, v);
            }
            catch (AssetConnectionException ex) {
                // TODO rethrow
            }
        });
    }

    @Override
    public OpcUaAssetConnectionConfig asConfig() {
        return null;
    }


    @Override
    public void registerValueProvider(Reference reference, OpcUaValueProviderConfig valueProviderConfig) throws AssetConnectionException {
        Class elementType = context.getElementType(reference);
        if (!DataElement.class.isAssignableFrom(elementType)) {
            throw new AssetConnectionException(String.format("unsupported submodel element type (%s)", elementType.getSimpleName()));
        }

        this.valueProviders.put(reference, new AssetValueProvider() {
            @Override
            public DataElementValue getValue() throws AssetConnectionException {
                try {
                    VariableNode node = client.getAddressSpace().getVariableNode(OpcUaUtils.parseNodeId(client, valueProviderConfig.getNodeId()));

                    DataElementValue newValue;
                    newValue = ContentParserFactory
                                .create()
                                .parseValue(node.getValue().getValue().getValue().toString(), elementType);
                    return newValue;
                } catch (UaException | AssetConnectionException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void setValue(DataElementValue value) throws AssetConnectionException {
                if(!(value instanceof PropertyValue)) {
                    throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
                }
                try {
                    VariableNode node = client.getAddressSpace().getVariableNode(OpcUaUtils.parseNodeId(client, valueProviderConfig.getNodeId()));
                    String datatypeName = BuiltinDataType.getBackingClass(node.getDataType()).getSimpleName();
                    client.writeValue(node.getNodeId(), new DataValue(
                            new Variant(
                                    castDatatype(value, datatypeName)
                                    ),null, null)).get();
                } catch (UaException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //this is a workaround until DataElementValue can return proper types
    private Object castDatatype(DataElementValue value, String datatypeName) {
        String valueString = ((PropertyValue) value).getValue();
        switch (datatypeName) {
            case "Double":
                return Double.valueOf(valueString);
            case "String":
                return valueString;
        }
        throw new UnsupportedOperationException("Datatype is not supported for writing to OPC.");
    }


    @Override
    public void registerOperationProvider(Reference reference, OpcUaOperationProviderConfig operationProviderConfig) {
        this.operationProviders.put(reference, new AssetOperationProvider() {
            @Override
            public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
                NodeId methodId = OpcUaUtils.parseNodeId(client, operationProviderConfig.getNodeId());
                // Automatically resolve parent node which is required for milo.
                // Currently, two equal (but not Objects.equals!) objects are returned, using simply the first one
                try {
                    NodeId objectId = client.getAddressSpace()
                            .getNode(methodId)
                            .browseNodes(AddressSpace.BrowseOptions.builder()
                                    .setBrowseDirection(BrowseDirection.Inverse)
                                    .build())
                            .get(0)
                            .getNodeId();
                    Variant[] parameters = Stream.of(input)
                            .map(x -> new Variant(x.getValue()))
                            .toArray(Variant[]::new);
                    CallMethodResult methodResult = client.call(new CallMethodRequest(
                            objectId,
                            methodId,
                            parameters
                    )).get();

                    //todo set output arguments
                    //methodResult.getOutputArguments()[i].getValue()

                } catch (UaException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return inoutput;
            }

            @Override
            public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callback) throws AssetConnectionException {
                throw new UnsupportedOperationException("not supported yet.");
            }
        });
    }


    @Override
    public void registerSubscriptionProvider(Reference reference, OpcUaSubscriptionProviderConfig subscriptionProviderConfig) throws AssetConnectionException {
        Class elementType = context.getElementType(reference);
        if (!DataElement.class.isAssignableFrom(elementType)) {
            throw new AssetConnectionException(String.format("unsupported submodel element type (%s)", elementType.getSimpleName()));
        }

        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) {
                try {
                    OpcUaUtils.subscribe(client,
                            OpcUaUtils.parseNodeId(client, subscriptionProviderConfig.getNodeId()), subscriptionProviderConfig.getInterval(), x -> {
                                try {
                                    listener.newDataReceived(ContentParserFactory
                                            .create()
                                            .parseValue(x.getValue().getValue().toString(), elementType));
                                } catch (AssetConnectionException e) {
                                    e.printStackTrace();
                                }
                            });
                } catch (InterruptedException|ExecutionException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void removeNewDataListener(NewDataListener listener) {
                try {
                    OpcUaUtils.unsubscribe(client, OpcUaUtils.parseNodeId(client, subscriptionProviderConfig.getNodeId()), subscriptionProviderConfig.getInterval());
                } catch (InterruptedException|ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void unregisterValueProvider(Reference reference) {
        this.valueProviders.remove(reference);
    }


    @Override
    public void unregisterOperationProvider(Reference reference) {
        this.operationProviders.remove(reference);
    }


    @Override
    public void unregisterSubscriptionProvider(Reference reference) {
        this.subscriptionProviders.remove(reference);
    }


    @Override
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return this.valueProviders;
    }


    @Override
    public Map<Reference, AssetOperationProvider> getOperationProviders() {
        return this.operationProviders;
    }


    @Override
    public Map<Reference, AssetSubscriptionProvider> getSubscriptionProviders() {
        return this.subscriptionProviders;
    }


    @Override
    public boolean sameAs(AssetConnection other) {
        return false;
    }

}
