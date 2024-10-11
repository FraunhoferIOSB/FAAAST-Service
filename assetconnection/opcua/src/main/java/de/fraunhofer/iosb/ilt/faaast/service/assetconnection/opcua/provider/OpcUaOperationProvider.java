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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.ArgumentMapping;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.milo.opcua.sdk.client.AddressSpace;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaNode;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.BrowseDirection;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.CallMethodResult;


/**
 * Implementation of OperationProvider for OPC UA asset connections. Supports executing AAS operations via OPC UA.
 */
public class OpcUaOperationProvider extends AbstractOpcUaProvider<OpcUaOperationProviderConfig> implements AssetOperationProvider<OpcUaOperationProviderConfig> {

    private NodeId nodeId;
    private NodeId parentNodeId;
    private Argument[] methodArguments;
    private Argument[] methodOutputArguments;
    private OperationVariable[] outputVariables;
    private List<ArgumentMapping> inputArgumentMappingList;
    private List<ArgumentMapping> outputArgumentMappingList;

    public OpcUaOperationProvider(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            OpcUaOperationProviderConfig providerConfig,
            ValueConverter valueConverter) throws AssetConnectionException, InvalidConfigurationException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        init();
    }


    @Override
    public OpcUaOperationProviderConfig getConfig() {
        return providerConfig;
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
            // Workaround to fix a problem when the optional InputArguments node is not present. 
            // In case of a UaException with StatusCode Bad_NotFound an exception must not be thrown.
            // Then the method simply has no input arguments.
            if ((e.getCause() instanceof UaException) && (((UaException) e.getCause()).getStatusCode().getValue() == StatusCodes.Bad_NotFound)) {
                return new Argument[0];
            }
            else {
                Thread.currentThread().interrupt();
                throw new AssetConnectionException(String.format("Could not read input arguments (nodeId: %s)",
                        providerConfig.getNodeId()),
                        e);
            }
        }
    }


    private Argument[] getOutputArguments(UaMethodNode node) throws AssetConnectionException {
        try {
            return node.readOutputArgumentsAsync().get() != null
                    ? node.readOutputArgumentsAsync().get()
                    : new Argument[0];
        }
        catch (InterruptedException | ExecutionException e) {
            if ((e.getCause() instanceof UaException) && (((UaException) e.getCause()).getStatusCode().getValue() == StatusCodes.Bad_NotFound)) {
                return new Argument[0];
            }
            else {
                Thread.currentThread().interrupt();
                throw new AssetConnectionException(String.format("Could not read output arguments (nodeId: %s)",
                        providerConfig.getNodeId()),
                        e);
            }
        }
    }


    private void init() throws AssetConnectionException {
        nodeId = OpcUaHelper.parseNodeId(client, providerConfig.getNodeId());
        final UaMethodNode methodNode = getMethodNode(nodeId);
        // treat empty string for parentNodeId like a null pointer
        parentNodeId = ((providerConfig.getParentNodeId() != null) && (!providerConfig.getParentNodeId().equals("")))
                ? OpcUaHelper.parseNodeId(client, providerConfig.getParentNodeId())
                : getParentNode(nodeId).getNodeId();
        inputArgumentMappingList = providerConfig.getInputArgumentMapping() != null ? providerConfig.getInputArgumentMapping() : new ArrayList<>();
        outputArgumentMappingList = providerConfig.getOutputArgumentMapping() != null ? providerConfig.getOutputArgumentMapping() : new ArrayList<>();
        methodArguments = getInputArguments(methodNode);
        methodOutputArguments = getOutputArguments(methodNode);
        try {
            outputVariables = serviceContext.getOperationOutputVariables(reference);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            throw new AssetConnectionException(
                    String.format(
                            "Operation could not be found in AAS model (reference: %s)",
                            ReferenceHelper.toString(reference)),
                    e);
        }
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
            String argumentNameMapped = mapInputArgumentNameToIdShort(argumentName);
            ElementValue parameterValue;
            if (inputParameters.containsKey(argumentNameMapped)) {
                parameterValue = inputParameters.get(argumentNameMapped);
            }
            else if (inoutputParameters.containsKey(argumentNameMapped)) {
                parameterValue = inoutputParameters.get(argumentNameMapped);
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
        try {
            ElementValueMapper.setValue(newProperty,
                    PropertyValue.builder()
                            .value(newValue)
                            .build());
        }
        catch (ValueMappingException ex) {
            throw new AssetConnectionException(String.format("Error updating value from asset connection (idShort: %s)", element.getIdShort()));
        }
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
        setInOutResult(inoutputParameters, inoutputs, methodResult);
        List<OperationVariable> list = new ArrayList<>();
        for (OperationVariable ovar: outputVariables) {
            String idShortMapped = mapOutputIdShortToArgumentName(ovar.getValue().getIdShort());
            if (hasOutputArgument(idShortMapped)) {
                list.add(convertOutput(getOutputArgument(methodResult, idShortMapped), ovar));
            }
        }
        return list.toArray(OperationVariable[]::new);
    }


    private void setInOutResult(Map<String, ElementValue> inoutputParameters, OperationVariable[] inoutputs, CallMethodResult methodResult) throws AssetConnectionException {
        for (var param: inoutputParameters.entrySet()) {
            String idShortMapped = mapOutputIdShortToArgumentName(param.getKey());
            if (hasOutputArgument(idShortMapped)) {
                Optional<OperationVariable> ov = Stream.of(inoutputs).filter(y -> Objects.equals(param.getKey(), y.getValue().getIdShort())).findAny();
                if (ov.isPresent()) {
                    try {
                        ElementValueMapper.setValue(ov.get().getValue(), new PropertyValue(valueConverter.convert(
                                getOutputArgument(methodResult, idShortMapped),
                                ((PropertyValue) param.getValue()).getValue().getDataType())));
                    }
                    catch (ValueMappingException ex) {
                        throw new AssetConnectionException(String.format("Error parsing operation inoutput parameter (idShort: %s)", idShortMapped));
                    }
                }
            }
        }
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
        return Objects.hash(super.hashCode(),
                nodeId,
                parentNodeId,
                Arrays.hashCode(methodArguments),
                Arrays.hashCode(methodOutputArguments),
                Arrays.hashCode(outputVariables));
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OpcUaOperationProvider)) {
            return false;
        }
        final OpcUaOperationProvider that = (OpcUaOperationProvider) obj;
        return super.equals(that)
                && Objects.equals(nodeId, that.nodeId)
                && Objects.equals(parentNodeId, that.parentNodeId)
                && Arrays.equals(methodArguments, that.methodArguments)
                && Arrays.equals(methodOutputArguments, that.methodOutputArguments)
                && Arrays.equals(outputVariables, that.outputVariables);
    }


    private String mapInputArgumentNameToIdShort(String argumentName) {
        Optional<ArgumentMapping> rv = inputArgumentMappingList.stream().filter(arg -> arg.getArgumentName().equals(argumentName)).findAny();
        return rv.isEmpty() ? argumentName : rv.get().getIdShort();
    }


    private String mapOutputIdShortToArgumentName(String idShort) {
        Optional<ArgumentMapping> rv = outputArgumentMappingList.stream().filter(arg -> arg.getIdShort().equals(idShort)).findAny();
        return rv.isEmpty() ? idShort : rv.get().getArgumentName();
    }
}
