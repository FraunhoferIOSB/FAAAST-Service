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

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConversionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.typing.ElementValueTypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnection}
 * for the OPC UA protocol.
 * <p>
 * All asset connection operations are supported.
 * <p>
 * This implementation currently only supports submodel elements of type
 * {@link io.adminshell.aas.v3.model.Property} resp.
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}.
 * <p>
 * This class uses a single underlying OPC UA connection.
 */
public class OpcUaAssetConnection implements AssetConnection<OpcUaAssetConnectionConfig, OpcUaValueProviderConfig, OpcUaOperationProviderConfig, OpcUaSubscriptionProviderConfig> {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaAssetConnection.class);
    private static final ValueConverter valueConverter = new ValueConverter();

    private static final String NODE_ID_SEPARATOR = ";";
    private static final String NS_PREFIX = "ns=";
    private OpcUaClient client;
    private OpcUaAssetConnectionConfig config;
    private ManagedSubscription opcUaSubscription;
    private Map<Reference, AssetOperationProvider> operationProviders;
    private ServiceContext serviceContext;
    private Map<Reference, AssetSubscriptionProvider> subscriptionProviders;
    private final Map<String, SubscriptionHandler> subscriptions;
    private Map<Reference, AssetValueProvider> valueProviders;

    private static void checkStatusCode(StatusCode statusCode, String errorMessage) throws AssetConnectionException {
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


    public OpcUaAssetConnection() {
        this.subscriptions = new HashMap<>();
        this.valueProviders = new HashMap<>();
        this.operationProviders = new HashMap<>();
        this.subscriptionProviders = new HashMap<>();
    }


    /**
     * Consutrctor to conveniently create and init asset connection from code.
     *
     * @param coreConfig core configuration
     * @param config asset connection configuration
     * @param serviceContext service context which this asset connection is
     *            running in
     * @throws ConfigurationInitializationException if initialization fails
     */
    protected OpcUaAssetConnection(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this();
        init(coreConfig, config, serviceContext);
    }


    @Override
    public OpcUaAssetConnectionConfig asConfig() {
        return config;
    }


    @Override
    public void close() throws AssetConnectionException {
        if (client != null) {
            for (var subscription: subscriptions.values()) {
                try {
                    subscription.dataItem.delete();
                }
                catch (UaException e) {
                    logger.info("unsubscribing from OPC UA asset connection on connection closing failed", e);
                }
            }
            subscriptions.clear();
            try {
                client.disconnect().get();
            }
            catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                throw new AssetConnectionException("error closing OPC UA asset connection", e);
            }
        }
    }


    private void connect() throws AssetConnectionException {
        try {
            client = OpcUaClient.create(
                    config.getHost(),
                    endpoints -> endpoints.stream()
                            .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                            .findFirst(),
                    configBuilder -> configBuilder
                            .setApplicationName(LocalizedText.english("AAS-Service"))
                            .setApplicationUri("urn:de:fraunhofer:iosb:aas:service")
                            .setIdentityProvider(AnonymousProvider.INSTANCE)
                            .setRequestTimeout(uint(1000))
                            .setAcknowledgeTimeout(uint(1000))
                            .build());
            client.connect().get();
            // without sleep bad timeout while waiting for acknowledge appears from time to time
            Thread.sleep(200);
            opcUaSubscription = ManagedSubscription.create(client);
        }
        catch (InterruptedException | ExecutionException | UaException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error opening OPC UA connection (endpoint: %s)", config.getHost()), e);
        }
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
    public Map<Reference, AssetValueProvider> getValueProviders() {
        return this.valueProviders;
    }


    @Override
    public void init(CoreConfig coreConfig, OpcUaAssetConnectionConfig config, ServiceContext context) throws ConfigurationInitializationException {
        this.serviceContext = context;
        this.config = config;
        try {
            connect();
            for (var provider: config.getValueProviders().entrySet()) {
                registerValueProvider(provider.getKey(), provider.getValue());
            }
            for (var provider: config.getOperationProviders().entrySet()) {
                registerOperationProvider(provider.getKey(), provider.getValue());
            }
            for (var provider: config.getSubscriptionProviders().entrySet()) {
                registerSubscriptionProvider(provider.getKey(), provider.getValue());
            }
        }
        catch (AssetConnectionException e) {
            throw new ConfigurationInitializationException("initializaing OPC UA asset connection failed", e);
        }
    }


    private NodeId parseNodeId(String nodeId) {
        Optional<String> ns = Stream.of(nodeId.split(NODE_ID_SEPARATOR))
                .filter(x -> x.startsWith(NS_PREFIX))
                .findFirst();
        int namespaceIndex = 0;
        if (ns.isPresent()) {
            String namespace = ns.get().replace(NS_PREFIX, "");
            try {
                namespaceIndex = Integer.parseUnsignedInt(namespace);
            }
            catch (NumberFormatException e) {
                UShort actualNamespaceIndex = client.getNamespaceTable().getIndex(namespace);
                if (actualNamespaceIndex == null) {
                    throw new IllegalArgumentException(String.format("could not resolve namespace '%s'", namespace));
                }
                namespaceIndex = actualNamespaceIndex.intValue();
            }
        }
        else {
            logger.debug("no namespace provided for node. Using default: ns=0 (nodeId: {}", nodeId);
        }
        return NodeId.parse(nodeId.replace(ns.get(), NS_PREFIX + namespaceIndex));
    }


    /**
     * {@inheritdoc}
     *
     * @throws AssetConnectionException if nodeId could not be parsed
     * @throws AssetConnectionException if nodeId does not refer to a method
     *             node
     * @throws AssetConnectionException if parent node of nodeId could not be
     *             resolved
     * @throws AssetConnectionException if output variables are null or do
     *             contain any other type than
     *             {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue}
     */
    @Override
    public void registerOperationProvider(Reference reference, OpcUaOperationProviderConfig providerConfig) throws AssetConnectionException {
        String baseErrorMessage = "error registering operation provider";
        final NodeId nodeId = parseNodeId(providerConfig.getNodeId());
        final UaNode node;
        try {
            node = client.getAddressSpace().getNode(nodeId);
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("%s - could not resolve nodeId (nodeId: %s)",
                    baseErrorMessage,
                    providerConfig.getNodeId()),
                    e);
        }
        if (!UaMethodNode.class.isAssignableFrom(node.getClass())) {
            throw new AssetConnectionException(String.format("%s - provided node must be a method (nodeId: %s",
                    baseErrorMessage,
                    providerConfig.getNodeId()));
        }
        final UaMethodNode methodNode = (UaMethodNode) node;
        final NodeId parentNodeId;
        try {
            parentNodeId = client.getAddressSpace()
                    .getNode(nodeId)
                    .browseNodes(AddressSpace.BrowseOptions.builder()
                            .setBrowseDirection(BrowseDirection.Inverse)
                            .build())
                    .get(0)
                    .getNodeId();
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("%s - could not resolve parent node (nodeId: %s)",
                    baseErrorMessage,
                    providerConfig.getNodeId()),
                    e);
        }
        final Argument[] methodArguments;
        try {
            methodArguments = methodNode.readInputArgumentsAsync().get() != null
                    ? methodNode.readInputArgumentsAsync().get()
                    : new Argument[0];
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("%s - could not read input arguments (nodeId: %s)",
                    baseErrorMessage,
                    providerConfig.getNodeId()),
                    e);
        }
        final Argument[] methodOutputArguments;
        try {
            methodOutputArguments = methodNode.readOutputArgumentsAsync().get() != null
                    ? methodNode.readOutputArgumentsAsync().get()
                    : new Argument[0];
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("%s - could not read ouput arguments (nodeId: %s)",
                    baseErrorMessage,
                    providerConfig.getNodeId()),
                    e);
        }
        final OperationVariable[] outputVariables = serviceContext.getOperationOutputVariables(reference) != null
                ? serviceContext.getOperationOutputVariables(reference)
                : new OperationVariable[0];
        for (var outputVariable: outputVariables) {
            if (outputVariable == null) {
                throw new AssetConnectionException(String.format("%s - output variable must be non-null (nodeId: %s)",
                        baseErrorMessage,
                        providerConfig.getNodeId()));
            }
            SubmodelElement submodelElement = outputVariable.getValue();
            if (submodelElement == null) {
                throw new AssetConnectionException(String.format("%s - output variable must contain non-null submodel element (nodeId: %s)",
                        baseErrorMessage,
                        providerConfig.getNodeId()));
            }
            if (!Property.class.isAssignableFrom(submodelElement.getClass())) {
                throw new AssetConnectionException(String.format("%s - unsupported element type (nodeId: %s, element type: %s)",
                        baseErrorMessage,
                        submodelElement.getClass(),
                        providerConfig.getNodeId()));
            }
        }

        this.operationProviders.put(reference, new AssetOperationProvider() {
            @Override
            public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
                String baseErrorMessage = "error invoking operation on asset connection";
                Map<String, ElementValue> inputParameter = new HashMap<>();
                if (input != null) {
                    try {
                        inputParameter = Stream.of(input).collect(Collectors.toMap(
                                x -> x.getValue().getIdShort(),
                                LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue()))));
                    }
                    catch (ValueMappingException e) {
                        throw new AssetConnectionException(
                                String.format("%s - could not exract value of input parameters",
                                        baseErrorMessage),
                                e);
                    }
                }

                Map<String, ElementValue> inoutputParameter = new HashMap<>();
                if (inoutput != null) {
                    try {
                        inoutputParameter = Stream.of(inoutput).collect(Collectors.toMap(
                                x -> x.getValue().getIdShort(),
                                LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue()))));
                    }
                    catch (ValueMappingException e) {
                        throw new AssetConnectionException(
                                String.format("%s - could not exract value of inoutput parameters",
                                        baseErrorMessage),
                                e);
                    }
                }
                if (methodArguments.length != (inputParameter.size() + inoutputParameter.size())) {
                    throw new AssetConnectionException(String.format("%s - argument count mismatch (expected: %d, provided input arguments: %d, provided inoutput arguments: %d)",
                            baseErrorMessage,
                            methodArguments.length,
                            inputParameter.size(),
                            inoutputParameter.size()));
                }
                Variant[] actualParameters = new Variant[methodArguments.length];
                for (int i = 0; i < methodArguments.length; i++) {
                    String argumentName = methodArguments[i].getName();
                    ElementValue parameterValue;
                    if (inputParameter.containsKey(argumentName)) {
                        parameterValue = inputParameter.get(argumentName);
                    }
                    else if (inoutputParameter.containsKey(argumentName)) {
                        parameterValue = inoutputParameter.get(argumentName);
                    }
                    else {
                        throw new AssetConnectionException(String.format("%s - missing argument (argument name: %s)",
                                baseErrorMessage,
                                argumentName));
                    }
                    if (parameterValue == null) {
                        throw new AssetConnectionException(String.format("%s - parameter value must be non-null (argument name: %s)",
                                baseErrorMessage,
                                argumentName));
                    }
                    if (!PropertyValue.class.isAssignableFrom(parameterValue.getClass())) {
                        throw new AssetConnectionException(String.format("%s - currently only parameters of the Property are supported (argument name: %s, provided type: %s)",
                                baseErrorMessage,
                                argumentName,
                                parameterValue.getClass()));
                    }
                    actualParameters[i] = valueConverter.convert(
                            ((PropertyValue) parameterValue).getValue(),
                            methodArguments[i].getDataType());
                }
                CallMethodResult methodResult;
                try {
                    methodResult = client.call(new CallMethodRequest(
                            parentNodeId,
                            nodeId,
                            actualParameters)).get();
                }
                catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new AssetConnectionException(String.format("%s - executing OPC UA method failed (nodeId: %s)",
                            baseErrorMessage,
                            providerConfig.getNodeId()),
                            e);
                }
                OperationVariable[] result = new OperationVariable[outputVariables.length];
                for (int i = 0; i < methodOutputArguments.length; i++) {
                    String argumentName = methodArguments[i].getName();
                    for (int j = 0; j < outputVariables.length; j++) {
                        if (Objects.equals(argumentName, outputVariables[j].getValue().getIdShort())) {
                            SubmodelElement element = outputVariables[j].getValue();
                            Datatype targetType;
                            try {
                                targetType = ((PropertyValue) ElementValueMapper.toValue(element)).getValue().getDataType();
                            }
                            catch (ValueMappingException e) {
                                throw new AssetConnectionException(
                                        String.format("%s - could not exract value of results variable with idShort '%s'",
                                                baseErrorMessage,
                                                element.getIdShort()),
                                        e);
                            }
                            TypedValue<?> newValue = valueConverter.convert(methodResult.getOutputArguments()[i], targetType);
                            Property newProperty = DeepCopyHelper.deepCopy(element, Property.class);
                            ElementValueMapper.setValue(newProperty,
                                    PropertyValue.builder()
                                            .value(newValue)
                                            .build());
                            result[j] = new DefaultOperationVariable.Builder()
                                    .value(newProperty)
                                    .build();
                        }
                    }
                    // update inoutput variable values
                    if (inoutputParameter.containsKey(argumentName) && inoutput != null) {
                        // find in original array and set there
                        for (int j = 0; j < inoutput.length; j++) {
                            if (Objects.equals(argumentName, inoutput[j].getValue().getIdShort())) {
                                ElementValueMapper.setValue(
                                        inoutput[j].getValue(),
                                        new PropertyValue(valueConverter.convert(
                                                methodResult.getOutputArguments()[i],
                                                ((PropertyValue) inoutputParameter.get(argumentName)).getValue().getDataType())));
                            }
                        }
                    }
                }
                return result;
            }
        });
    }


    /**
     * {@inheritdoc}
     *
     * @throws AssetConnectionException if reference does not point to a
     *             {@link io.adminshell.aas.v3.model.Property}
     * @throws AssetConnectionException if referenced
     *             {@link io.adminshell.aas.v3.model.Property} does not have datatype
     *             defined
     */
    @Override
    public void registerSubscriptionProvider(Reference reference, OpcUaSubscriptionProviderConfig providerConfig) throws AssetConnectionException {
        final String baseErrorMessage = "error registering subscription provider";
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
        final Datatype datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("%s - missing datatype (reference: %s)",
                    baseErrorMessage,
                    AasUtils.asString(reference)));
        }
        this.subscriptionProviders.put(reference, new AssetSubscriptionProvider() {
            @Override
            public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
                if (!subscriptions.containsKey(providerConfig.getNodeId())) {
                    SubscriptionHandler handler = new SubscriptionHandler();
                    handler.datatype = datatype;
                    try {
                        handler.originalValue = client.readValue(0, TimestampsToReturn.Neither,
                                client.getAddressSpace().getVariableNode(parseNodeId(providerConfig.getNodeId())).getNodeId()).get();
                    }
                    catch (UaException | InterruptedException | ExecutionException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("{} - reading initial value of subscribed node failed (reference: {}, nodeId: {})",
                                baseErrorMessage,
                                AasUtils.asString(reference),
                                providerConfig.getNodeId(),
                                e);
                    }
                    try {
                        handler.dataItem = opcUaSubscription.createDataItem(
                                parseNodeId(providerConfig.getNodeId()),
                                LambdaExceptionHelper.rethrowConsumer(
                                        x -> x.addDataValueListener(LambdaExceptionHelper.rethrowConsumer(handler::notify))));
                    }
                    catch (UaException e) {
                        logger.warn("{} - could not create subscrption item (reference: {}, nodeId: {})",
                                baseErrorMessage,
                                AasUtils.asString(reference),
                                providerConfig.getNodeId(),
                                e);
                    }
                    subscriptions.put(providerConfig.getNodeId(), handler);
                }
                List<NewDataListener> listeners = subscriptions.get(providerConfig.getNodeId()).listeners;
                if (!listeners.contains(listener)) {
                    listeners.add(listener);
                }
            }


            @Override
            public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {

                if (subscriptions.containsKey(providerConfig.getNodeId())) {
                    SubscriptionHandler handler = subscriptions.get(providerConfig.getNodeId());
                    if (handler.listeners.contains(listener)) {
                        handler.listeners.remove(listener);
                    }
                    if (handler.listeners.isEmpty()) {
                        try {
                            handler.dataItem.delete();
                            subscriptions.remove(providerConfig.getNodeId());
                        }
                        catch (UaException e) {
                            throw new AssetConnectionException(
                                    String.format("%s - removing subscription failed (reference: %s, nodeId: %s)",
                                            baseErrorMessage,
                                            AasUtils.asString(reference),
                                            providerConfig.getNodeId()),
                                    e);
                        }
                    }
                }

            }
        });
    }


    /**
     * {@inheritdoc}
     *
     * @throws AssetConnectionException if reference does not point to a
     *             {@link io.adminshell.aas.v3.model.Property}
     * @throws AssetConnectionException if referenced
     *             {@link io.adminshell.aas.v3.model.Property} does not have datatype
     *             defined
     * @throws AssetConnectionException if nodeId could not be parsed
     */
    @Override
    public void registerValueProvider(Reference reference, OpcUaValueProviderConfig providerConfig) throws AssetConnectionException {
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
        final Datatype datatype = valueTypeInfo.getDatatype();
        if (datatype == null) {
            throw new AssetConnectionException(String.format("%s - missing datatype (reference: %s)",
                    baseErrorMessage,
                    AasUtils.asString(reference)));
        }
        final VariableNode node;
        try {
            node = client.getAddressSpace().getVariableNode(parseNodeId(providerConfig.getNodeId()));
        }
        catch (UaException e) {
            throw new AssetConnectionException(
                    String.format("%s - could not parse nodeId (nodeId: %s)",
                            baseErrorMessage,
                            providerConfig.getNodeId()),
                    e);
        }
        this.valueProviders.put(reference, new AssetValueProvider() {
            @Override
            public DataElementValue getValue() throws AssetConnectionException {
                try {
                    DataValue dataValue = client.readValue(0, TimestampsToReturn.Neither, node.getNodeId()).get();
                    checkStatusCode(dataValue.getStatusCode(), "error reading value from asset conenction");
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
                    checkStatusCode(result, "error setting value on asset connection");
                }
                catch (InterruptedException | ExecutionException e) {
                    Thread.currentThread().interrupt();
                    throw new AssetConnectionException("error writing asset connection value", e);
                }
            }
        });
    }


    @Override
    public boolean sameAs(AssetConnection other) {
        return false;
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
    public void unregisterValueProvider(Reference reference) {
        this.valueProviders.remove(reference);
    }

    private static class SubscriptionHandler {

        ManagedDataItem dataItem;
        Datatype datatype;
        List<NewDataListener> listeners = new ArrayList<>();
        boolean firstCall = true;
        DataValue originalValue = null;

        public void notify(DataValue value) throws ValueConversionException {
            try {
                if (!firstCall || (originalValue != null && !Objects.equals(value.getValue(), originalValue.getValue()))) {
                    DataElementValue newAasValue = new PropertyValue(valueConverter.convert(value.getValue(), datatype));
                    for (var listener: listeners) {
                        try {
                            listener.newDataReceived(newAasValue);
                        }
                        catch (Exception e) {
                            logger.debug("exception while invoking newDataReceived handler", e);
                        }
                    }
                }
            }
            finally {
                firstCall = false;
            }
        }
    }
}
