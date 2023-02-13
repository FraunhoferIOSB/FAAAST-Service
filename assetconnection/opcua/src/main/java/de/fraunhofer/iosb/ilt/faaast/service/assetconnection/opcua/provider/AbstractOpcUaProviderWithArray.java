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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.AbstractOpcUaProviderWithArrayConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.Reference;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.core.nodes.VariableNode;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;


/**
 * Superclass for all OPC UA provider classes.
 *
 * @param <T> type of the asset provider config
 */
public abstract class AbstractOpcUaProviderWithArray<T extends AbstractOpcUaProviderWithArrayConfig> extends AbstractOpcUaProvider<T> {

    protected static final String REGEX_ARRAY_INDEX = "\\[([0-9]+)\\]";
    protected static final String REGEX_ARRAY_EXPRESSION = String.format("^(?>%s)+$", REGEX_ARRAY_INDEX);
    protected static final Pattern REGEX_PATTERN_ARRAY_INDEX = Pattern.compile(REGEX_ARRAY_INDEX);
    protected int[] arrayIndex;
    protected VariableNode node;

    protected AbstractOpcUaProviderWithArray(ServiceContext serviceContext,
            OpcUaClient client,
            Reference reference,
            T providerConfig,
            ValueConverter valueConverter) throws InvalidConfigurationException, AssetConnectionException {
        super(serviceContext, client, reference, providerConfig, valueConverter);
        parseArrayIndices(providerConfig.getArrayElementIndex());
        validate();
    }


    /**
     * Unwraps value in terms of returning the sub-value based on index if set, otherwise returns unmodified value.
     *
     * @param dataValue the value to process
     * @return the unwrapped value
     */
    public Variant unwrapValue(DataValue dataValue) {
        return hasArrayIndex()
                ? new Variant(getArrayElement(dataValue.getValue().getValue()))
                : dataValue.getValue();
    }


    /**
     * Return wether a valid array index has been defined for this provider.
     *
     * @return true if there is a valid array index, false otherwise
     */
    protected boolean hasArrayIndex() {
        return Objects.nonNull(arrayIndex) && arrayIndex.length > 0;
    }


    private void validate() throws InvalidConfigurationException {
        Ensure.require(
                Objects.equals(NodeClass.Variable, super.node.getNodeClass())
                        && VariableNode.class.isAssignableFrom(super.node.getClass()),
                new InvalidConfigurationException(
                        String.format("nodeId does not point to a variable node (nodeId: %s, node type: %s)",
                                providerConfig.getNodeId(),
                                super.node.getNodeClass())));
        this.node = (VariableNode) super.node;
        UInteger[] actualArrayDimensions = ((VariableNode) node).getArrayDimensions();
        if (hasArrayIndex() && arrayIndex.length > actualArrayDimensions.length) {
            throw new InvalidConfigurationException(
                    String.format("provided array index has more dimensions than the corresponding node (provided dimensions: %d, actual dimensions: %d, nodeId: %s)",
                            arrayIndex.length,
                            actualArrayDimensions.length,
                            providerConfig.getNodeId()));
        }
    }


    private void parseArrayIndices(String index) throws InvalidConfigurationException {
        if (Objects.isNull(index) || index.isBlank()) {
            return;
        }
        if (!index.matches(REGEX_ARRAY_EXPRESSION)) {
            throw new InvalidConfigurationException(String.format("invalid array index expression (expression: %s, expected format: %s)",
                    index,
                    REGEX_ARRAY_EXPRESSION));
        }
        arrayIndex = REGEX_PATTERN_ARRAY_INDEX.matcher(index).results()
                .mapToInt(x -> Integer.parseInt(x.group(1)))
                .toArray();
    }


    private String indexPartToString(int depth) {
        Ensure.requireNonNull(depth, "depth must be non-null");
        Ensure.requireNonNull(arrayIndex, "index must be non-null");
        return Stream.of(arrayIndex)
                .limit(depth)
                .map(Object::toString)
                .collect(Collectors.joining("", "[", "]"));
    }


    private String indexToString() {
        return indexPartToString(arrayIndex.length);
    }


    /**
     * Sets the given value in the desired element of the given array.
     *
     * @param array The original array.
     * @param newValue The desired value.
     * @throws NullPointerException if an intermediate element is null
     * @throws ClassCastException if an intermediate element is not an array
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    protected void setArrayElement(Object array, Object newValue) {
        Array.set(navigateToIndex(array), arrayIndex[arrayIndex.length - 1], newValue);
    }


    private Object navigateToIndex(Object obj) {
        if (Objects.isNull(arrayIndex)) {
            return obj;
        }
        Object result = obj;
        for (int i = 0; i < arrayIndex.length - 1; i++) {
            if (Objects.isNull(result)) {
                throw new NullPointerException(String.format(
                        "error accessing array at given index - intermediate element is null (requested index: %s, index with object being null: )",
                        indexToString(),
                        indexPartToString(i + 1)));
            }
            if (!result.getClass().isArray()) {
                throw new ClassCastException(String.format(
                        "error accessing array at given index - intermediate element not an array (requested index: %s, index with non-array object: )",
                        indexToString(),
                        indexPartToString(i + 1)));
            }
            result = Array.get(result, arrayIndex[i]);
        }
        if (!result.getClass().isArray()) {
            throw new ClassCastException(String.format(
                    "error accessing array at given index - intermediate element not an array (requested index: %s, index with non-array object: )",
                    indexToString(),
                    indexPartToString(arrayIndex.length - 1)));
        }
        return result;
    }


    /**
     * Gets the given value at array index of the given array.
     *
     * @param array The original array.
     * @throws NullPointerException if an intermediate element is null
     * @throws ClassCastException if an intermediate element is not an array
     * @throws ArrayIndexOutOfBoundsException if index is out of bounds
     */
    protected Object getArrayElement(Object array) {
        return Array.get(navigateToIndex(array), arrayIndex[arrayIndex.length - 1]);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), arrayIndex);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractOpcUaProviderWithArray)) {
            return false;
        }
        final AbstractOpcUaProviderWithArray<?> that = (AbstractOpcUaProviderWithArray<?>) obj;
        return super.equals(obj)
                && Arrays.equals(arrayIndex, that.arrayIndex);
    }

}
