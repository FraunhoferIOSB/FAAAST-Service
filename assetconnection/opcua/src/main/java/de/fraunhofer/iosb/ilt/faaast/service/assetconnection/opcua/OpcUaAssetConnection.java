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
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.*;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;


/**
 * Implementation of OPC UA Asset Connection
 */
public class OpcUaAssetConnection
        implements AssetConnection<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    private OpcUaAssetConnectionConfig config;
    private Map<Reference, AssetValueProvider> valueProviders;
    private Map<Reference, AssetOperationProvider> operationProviders;
    private Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private ServiceContext context;
    private final String NODE_ID_SEPARATOR = ";";
    private final String NS_PREFIX = "ns=";

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
        this.context = context;
        this.config = config;

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
        this.valueProviders.put(reference, new AssetValueProvider() {
            @Override
            public DataElementValue getValue() throws AssetConnectionException {
                try {
                    OpcUaClient client = createClient(config.getHost(), AnonymousProvider.INSTANCE);
                    client.connect().get();
                    NodeId valueNodeId = parseNodeId(client, valueProviderConfig.getNodeId());
                    Object value = client.getAddressSpace().getVariableNode(valueNodeId).getValue().getValue().getValue();
                    client.disconnect().get();
                    DataElementValue newValue = null;
                    Class elementType = context.getElementType(reference);
                    if (!DataElement.class.isAssignableFrom(elementType)) {
                        throw new AssetConnectionException(String.format("unsupported submodel element type (%s)", elementType.getSimpleName()));
                    }
                    newValue = ContentParserFactory
                                .create()
                                .parseValue(value.toString(), elementType);
                    return newValue;
                } catch (UaException | AssetConnectionException | InterruptedException | ExecutionException e) {
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
                    OpcUaClient client = createClient(config.getHost(), AnonymousProvider.INSTANCE);
                    client.connect().get();
                    VariableNode node = client.getAddressSpace().getVariableNode(parseNodeId(client, valueProviderConfig.getNodeId()));
                    String datatypeName = BuiltinDataType.getBackingClass(node.getDataType()).getSimpleName();
                    client.writeValue(node.getNodeId(), new DataValue(
                            new Variant(
                                    castDatatype(value, datatypeName)
                                    ),null, null)).get();
                    client.disconnect().get();
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
            case "Long":
                return Long.valueOf(valueString);
            case "Boolean":
                return Boolean.valueOf(valueString);
            case "Float":
                return Float.valueOf(valueString);
            case "Integer":
                return Integer.valueOf(valueString);
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
                // Automatically resolve parent node which is required for milo.
                // Currently, two equal (but not Objects.equals!) objects are returned, using simply the first one
                try {
                    OpcUaClient client = createClient(config.getHost(), AnonymousProvider.INSTANCE);
                    client.connect().get();
                    NodeId methodId = parseNodeId(client, operationProviderConfig.getNodeId());
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
                    client.disconnect().get();
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
        OpcUaClient client = null;
        try {
            client = createClient(config.getHost(), AnonymousProvider.INSTANCE);
        } catch (UaException e) {
            e.printStackTrace();
        }
        OpcUaClient finalClient = client;
        try {
            finalClient.connect().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) {
                try {
                    subscribe(finalClient,
                            parseNodeId(finalClient, subscriptionProviderConfig.getNodeId()), subscriptionProviderConfig.getInterval(), x -> {
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
                    unsubscribe(finalClient, parseNodeId(finalClient, subscriptionProviderConfig.getNodeId()), subscriptionProviderConfig.getInterval());
                    finalClient.disconnect().get();
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

    public NodeId parseNodeId(OpcUaClient client, String nodeId) {
        Optional<String> ns = Stream.of(nodeId.split(NODE_ID_SEPARATOR))
                .filter(x -> x.startsWith(NS_PREFIX))
                .findFirst();
        int namespaceIndex = 0;
        if (ns.isPresent()) {
            String namespace = ns.get().replace(NS_PREFIX, "");
            try {
                namespaceIndex = Integer.parseUnsignedInt(namespace);
            } catch (NumberFormatException ex) {
                UShort actualNamespaceIndex = client.getNamespaceTable().getIndex(namespace);
                if (actualNamespaceIndex == null) {
                    throw new RuntimeException(String.format("could not resolve namespace '%s'", namespace));
                }
                namespaceIndex = actualNamespaceIndex.intValue();
            }
        } else {
            System.out.println("no namespace provided for node. Using default (ns=0)");
        }
        return NodeId.parse(nodeId.replace(ns.get(), NS_PREFIX + namespaceIndex));
    }

    public OpcUaClient createClient(String opcUrl, IdentityProvider identityProvider) throws UaException {
        return createClient(URI.create(opcUrl), identityProvider);
    }

    public OpcUaClient createClient(URI opcUrl, IdentityProvider identityProvider) throws UaException {
        return OpcUaClient.create(
                opcUrl.toString(),
                endpoints ->
                        endpoints.stream()
                                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                .findFirst(),
                configBuilder
                        -> configBuilder
                        .setApplicationName(LocalizedText.english("AAS-Service"))
                        .setApplicationUri("urn:de:fraunhofer:iosb:aas:service")
                        .setIdentityProvider(identityProvider)
                        .setRequestTimeout(uint(5000))
                        .build()
        );
    }

    public UaSubscription subscribe(OpcUaClient client, NodeId node, double interval, Consumer<DataValue> consumer) throws InterruptedException, ExecutionException {
        UaSubscription result = null;
        List<UaMonitoredItem> items = null;
        ReadValueId readValueId = new ReadValueId(node, AttributeId.Value.uid(), null, null);
        UInteger clientHandle = uint(new Random().nextInt((int) Math.min(UInteger.MAX.longValue(), (long) Integer.MAX_VALUE)));
        MonitoringParameters monitorParameters = new MonitoringParameters(clientHandle, 1000.0, null, uint(10), true);
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, monitorParameters);
        result = client.getSubscriptionManager().createSubscription(interval).get();
        items = result.createMonitoredItems(
                TimestampsToReturn.Both,
                Arrays.asList(request),
                (monitoredItem, id) -> {
                    monitoredItem.setValueConsumer(consumer);
                }).get();
        return result;
    }

    public UaSubscription unsubscribe(OpcUaClient client, NodeId node, double interval) throws InterruptedException, ExecutionException {
        UaSubscription result = null;
        List<UaMonitoredItem> items = null;

        ReadValueId readValueId = new ReadValueId(node, AttributeId.Value.uid(), null, null);
        UInteger clientHandle = uint(new Random().nextInt((int) Math.min(UInteger.MAX.longValue(), (long) Integer.MAX_VALUE)));

        for (UaSubscription u: client.getSubscriptionManager().getSubscriptions()
        ) {
            MonitoringParameters monitorParameters = new MonitoringParameters(clientHandle, interval, null, uint(10), true);
            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, monitorParameters);

            items = result.createMonitoredItems(
                    TimestampsToReturn.Both,
                    Arrays.asList(request)).get();

            if(u.getMonitoredItems().contains(items)) {
                u.deleteMonitoredItems(items).get();
            }
        }
        return result;
    }

}
