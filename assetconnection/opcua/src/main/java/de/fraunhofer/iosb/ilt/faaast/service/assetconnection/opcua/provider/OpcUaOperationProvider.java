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
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import java.util.HashMap;
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
public class OpcUaOperationProvider extends AbstractOpcUaProvider<OpcUaOperationProviderConfig> implements AssetOperationProvider {

    private NodeId nodeId;
    private NodeId parentNodeId;
    private Argument[] methodArguments;
    private Argument[] methodOutputArguments;
    private OperationVariable[] outputVariables;

    public OpcUaOperationProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaOperationProviderConfig providerConfig,
            ValueConverter valueConverter) throws AssetConnectionException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        init();
    }


    private UaMethodNode getMethodNode(NodeId nodeId) throws AssetConnectionException {
        try {
            UaNode node = client.getAddressSpace().getNode(nodeId);
            if (!UaMethodNode.class.isAssignableFrom(node.getClass())) {
                throw new AssetConnectionException(String.format("Provided node must be a method (nodeId: %s",
                        providerConfig.getNodeId()));
            }
            return (UaMethodNode) node;
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("Could not resolve nodeId (nodeId: %s)",
                    providerConfig.getNodeId()),
                    e);
        }
    }


    private UaNode getParentNode(NodeId nodeId) throws AssetConnectionException {
        try {
            return client.getAddressSpace()
                    .getNode(nodeId)
                    .browseNodes(AddressSpace.BrowseOptions.builder()
                            .setBrowseDirection(BrowseDirection.Inverse)
                            .build())
                    .get(0);
        }
        catch (UaException e) {
            throw new AssetConnectionException(String.format("Could not resolve parent node (nodeId: %s)",
                    providerConfig.getNodeId()),
                    e);
        }
    }


    private Argument[] getInputArguments(UaMethodNode node) throws AssetConnectionException {
        try {
            return node.readInputArgumentsAsync().get() != null
                    ? node.readInputArgumentsAsync().get()
                    : new Argument[0];
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("Could not read input arguments (nodeId: %s)",
                    providerConfig.getNodeId()),
                    e);
        }
    }


    private Argument[] getOutputArguments(UaMethodNode node) throws AssetConnectionException {
        try {
            return node.readOutputArgumentsAsync().get() != null
                    ? node.readOutputArgumentsAsync().get()
                    : new Argument[0];
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("Could not read output arguments (nodeId: %s)",
                    providerConfig.getNodeId()),
                    e);
        }
    }


    private void init() throws AssetConnectionException {
        nodeId = OpcUaHelper.parseNodeId(client, providerConfig.getNodeId());
        final UaMethodNode methodNode = getMethodNode(nodeId);
        parentNodeId = getParentNode(nodeId).getNodeId();
        methodArguments = getInputArguments(methodNode);
        methodOutputArguments = getOutputArguments(methodNode);
        outputVariables = serviceContext.getOperationOutputVariables(reference) != null
                ? serviceContext.getOperationOutputVariables(reference)
                : new OperationVariable[0];
        for (var outputVariable: outputVariables) {
            if (outputVariable == null) {
                throw new AssetConnectionException(String.format("Output variable must be non-null (nodeId: %s)",
                        providerConfig.getNodeId()));
            }
            SubmodelElement submodelElement = outputVariable.getValue();
            if (submodelElement == null) {
                throw new AssetConnectionException(String.format("Output variable must contain non-null submodel element (nodeId: %s)",
                        providerConfig.getNodeId()));
            }
            if (!Property.class.isAssignableFrom(submodelElement.getClass())) {
                throw new AssetConnectionException(String.format("Unsupported element type (nodeId: %s, element type: %s)",
                        submodelElement.getClass(),
                        providerConfig.getNodeId()));
            }
        }
    }


    private Map<String, ElementValue> parseParameters(OperationVariable[] parameters) throws AssetConnectionException {
        if (parameters == null) {
            return new HashMap<>();
        }
        try {
            return Stream.of(parameters).collect(Collectors.toMap(
                    x -> x.getValue().getIdShort(),
                    LambdaExceptionHelper.rethrowFunction(x -> ElementValueMapper.toValue(x.getValue()))));
        }
        catch (ValueMappingException e) {
            throw new AssetConnectionException("Could not extract value of parameters", e);
        }
    }


    private Variant[] convertParameters(Map<String, ElementValue> inputParameters, Map<String, ElementValue> inoutputParameters) throws AssetConnectionException {
        Variant[] result = new Variant[methodArguments.length];
        for (int i = 0; i < methodArguments.length; i++) {
            String argumentName = methodArguments[i].getName();
            ElementValue parameterValue;
            if (inputParameters.containsKey(argumentName)) {
                parameterValue = inputParameters.get(argumentName);
            }
            else if (inoutputParameters.containsKey(argumentName)) {
                parameterValue = inoutputParameters.get(argumentName);
            }
            else {
                throw new AssetConnectionException(String.format("Missing argument (argument name: %s)",
                        argumentName));
            }
            if (parameterValue == null) {
                throw new AssetConnectionException(String.format("Parameter value must be non-null (argument name: %s)",
                        argumentName));
            }
            if (!PropertyValue.class.isAssignableFrom(parameterValue.getClass())) {
                throw new AssetConnectionException(String.format("Currently only parameters of the Property are supported (argument name: %s, provided type: %s)",
                        argumentName,
                        parameterValue.getClass()));
            }
            result[i] = valueConverter.convert(
                    ((PropertyValue) parameterValue).getValue(),
                    methodArguments[i].getDataType());
        }
        return result;
    }


    private OperationVariable convertOutput(Variant value, OperationVariable typeInfo) throws AssetConnectionException {
        SubmodelElement element = typeInfo.getValue();
        Datatype targetType;
        try {
            targetType = ((PropertyValue) ElementValueMapper.toValue(element)).getValue().getDataType();
        }
        catch (ValueMappingException e) {
            throw new AssetConnectionException(
                    String.format("Could not exract value of results variable with idShort '%s'",
                            element.getIdShort()),
                    e);
        }
        TypedValue<?> newValue = valueConverter.convert(value, targetType);
        Property newProperty = DeepCopyHelper.deepCopy(element, Property.class);
        ElementValueMapper.setValue(newProperty,
                PropertyValue.builder()
                        .value(newValue)
                        .build());
        return new DefaultOperationVariable.Builder()
                .value(newProperty)
                .build();
    }


    private boolean hasOutputArgument(String name) {
        return Stream.of(methodOutputArguments).anyMatch(x -> Objects.equals(x.getName(), name));
    }


    private Variant getOutputArgument(CallMethodResult methodResult, String name) {
        for (int i = 0; i < methodOutputArguments.length; i++) {
            if (Objects.equals(methodOutputArguments[i].getName(), name)) {
                return methodResult.getOutputArguments()[i];
            }
        }
        return null;
    }


    private OperationVariable[] convertResult(CallMethodResult methodResult, Map<String, ElementValue> inoutputParameters, OperationVariable[] inoutputs)
            throws AssetConnectionException {
        inoutputParameters.entrySet().stream()
                .filter(x -> hasOutputArgument(x.getKey()))
                .forEach(LambdaExceptionHelper.rethrowConsumer(x -> ElementValueMapper.setValue(
                        Stream.of(inoutputs).filter(y -> Objects.equals(x.getKey(), y.getValue().getIdShort())).findAny().get().getValue(),
                        new PropertyValue(valueConverter.convert(
                                getOutputArgument(methodResult, x.getKey()),
                                ((PropertyValue) x.getValue()).getValue().getDataType())))));
        return Stream.of(outputVariables)
                .map(LambdaExceptionHelper.rethrowFunction(
                        x -> hasOutputArgument(x.getValue().getIdShort())
                                ? convertOutput(getOutputArgument(methodResult, x.getValue().getIdShort()), x)
                                : null))
                .toArray(OperationVariable[]::new);
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        final Map<String, ElementValue> inputParameter = parseParameters(input);
        final Map<String, ElementValue> inoutputParameter = parseParameters(inoutput);
        Variant[] actualParameters = convertParameters(inputParameter, inoutputParameter);
        CallMethodResult methodResult;
        try {
            methodResult = client.call(new CallMethodRequest(
                    parentNodeId,
                    nodeId,
                    actualParameters)).get();
        }
        catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("Executing OPC UA method failed (nodeId: %s)",
                    providerConfig.getNodeId()),
                    e);
        }
        return convertResult(methodResult, inoutputParameter, inoutput);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), nodeId, parentNodeId, methodArguments, methodOutputArguments, outputVariables);
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
        final OpcUaOperationProvider that = (OpcUaOperationProvider) obj;
        return super.equals(that)
                && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(parentNodeId, that.parentNodeId)
                && Objects.equals(methodArguments, that.methodArguments)
                && Objects.equals(methodOutputArguments, that.methodOutputArguments)
                && Objects.equals(outputVariables, that.outputVariables);
    }
}
