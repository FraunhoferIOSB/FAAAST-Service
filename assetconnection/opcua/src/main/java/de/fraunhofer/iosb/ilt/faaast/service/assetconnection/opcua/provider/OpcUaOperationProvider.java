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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;


/**
 * Implemenation of OperationProvider for OPC UA asset connections. Supports
 * executing AAS operations via OPC UA.
 */
public class OpcUaOperationProvider implements AssetOperationProvider {

    private final ServiceContext serviceContext;
    private final OpcUaClient client;
    private final Reference reference;
    private final OpcUaOperationProviderConfig providerConfig;
    private final ValueConverter valueConverter;
    private NodeId nodeId;
    private NodeId parentNodeId;
    private Argument[] methodArguments;
    private Argument[] methodOutputArguments;
    private OperationVariable[] outputVariables;

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
    public OpcUaOperationProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaOperationProviderConfig providerConfig,
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
        String baseErrorMessage = "error registering operation provider";
        nodeId = OpcUaHelper.parseNodeId(client, providerConfig.getNodeId());
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
        outputVariables = serviceContext.getOperationOutputVariables(reference) != null
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
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        String baseErrorMessage = "error invoking operation on asset connection";
        final Map<String, ElementValue> inputParameter;
        if (input != null) {
            try {
                inputParameter = Stream.of(input).collect(Collectors.toMap(
                        x -> x.getValue().getIdShort(),
                        LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue()))));
            }
            catch (ValueMappingException e) {
                throw new AssetConnectionException(
                        String.format("%s - could not exract value of input parameters", baseErrorMessage),
                        e);
            }
        }
        else {
            inputParameter = new HashMap<>();
        }

        final Map<String, ElementValue> inoutputParameter;
        if (inoutput != null) {
            try {
                inoutputParameter = Stream.of(inoutput).collect(Collectors.toMap(
                        x -> x.getValue().getIdShort(),
                        LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue()))));
            }
            catch (ValueMappingException e) {
                throw new AssetConnectionException(
                        String.format("%s - could not exract value of inoutput parameters", baseErrorMessage),
                        e);
            }
        }
        else {
            inoutputParameter = new HashMap<>();
        }
        List<String> missingArguments = Stream.of(methodArguments)
                .map(Argument::getName)
                .filter(x -> !inputParameter.containsKey(x) && !inoutputParameter.containsKey(x))
                .collect(Collectors.toList());
        if (!missingArguments.isEmpty()) {
            throw new AssetConnectionException(
                    String.format("%s - missing required input argument(s): %s",
                            baseErrorMessage,
                            String.join(", ", missingArguments)));
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
            String argumentName = methodOutputArguments[i].getName();
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
}
