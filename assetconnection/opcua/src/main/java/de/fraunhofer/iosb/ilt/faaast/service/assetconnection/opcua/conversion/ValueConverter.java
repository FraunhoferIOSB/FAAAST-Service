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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.conversion;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.milo.opcua.sdk.server.events.conversions.ImplicitConversions;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


/**
 * Converts values bi-directional between OPC UA and AAS types.
 */
public class ValueConverter {

    private static final String NOT_ENOUGH_DIMENSION_TXT = "value is not an array or not enough dimensions";
    private Map<ConversionTypeInfo, AasToOpcUaValueConverter> aasToOpcUaConverters;
    private Map<ConversionTypeInfo, OpcUaToAasValueConverter> opcUaToAasConverters;
    private Pattern arrayPattern;

    public ValueConverter() {
        this.aasToOpcUaConverters = new HashMap<>();
        this.opcUaToAasConverters = new HashMap<>();
        arrayPattern = Pattern.compile("(?<=\\[).*?(?=\\])");
        register(Datatype.INTEGER, Identifiers.Integer, new AasToOpcUaValueConverter() {
            @Override
            public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
                return new Variant(((BigInteger) value.getValue()).intValueExact());
            }
        });
    }


    /**
     * Registers an AAS to OPC UA mapping.
     *
     * @param aasDatatype AAS datatype to map
     * @param opcUaDatatype OPC UA target datatype
     * @param conveter actual converter implementation
     */
    public void register(Datatype aasDatatype, NodeId opcUaDatatype, AasToOpcUaValueConverter conveter) {
        aasToOpcUaConverters.put(new ConversionTypeInfo(aasDatatype, opcUaDatatype), conveter);
    }


    /**
     * Registers an OPC UA to AAS mapping.
     *
     * @param aasDatatype AAS target datatype
     * @param opcUaDatatype OPC UA datatype to map
     * @param conveter actual converter implementation
     */
    public void register(Datatype aasDatatype, NodeId opcUaDatatype, OpcUaToAasValueConverter conveter) {
        opcUaToAasConverters.put(new ConversionTypeInfo(aasDatatype, opcUaDatatype), conveter);
    }


    /**
     * Converts AAS value to OPC UA target type.
     *
     * @param value AAS value
     * @param targetType OPC UA target type
     * @return converted AAS value
     * @throws ValueConversionException if value or targetType are null or conversion fails
     */
    public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
        if (value == null) {
            throw new ValueConversionException("typed value must be non-null");
        }
        if (targetType == null) {
            throw new ValueConversionException("type value must be non-null");
        }
        AasToOpcUaValueConverter converter = aasToOpcUaConverters.getOrDefault(
                new ConversionTypeInfo(value.getDataType(), targetType),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }


    /**
     * Converts OPC UA value to AAS target type.
     *
     * @param value OPC UAvalue
     * @param targetType AAS target type
     * @return converted OPC UA value
     * @throws ValueConversionException if value or targetType are null or conversion fails
     */
    public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
        if (value == null) {
            throw new ValueConversionException("value must be non-null");
        }
        if (targetType == null) {
            throw new ValueConversionException("targetType value must be non-null");
        }
        if (value.getDataType().isEmpty()) {
            throw new ValueConversionException(String.format("unable to determine datatype of OPC UA value (value: %s)", value));
        }
        Optional<NodeId> valueDatatype = value.getDataType().get().toNodeId(null);
        if (valueDatatype.isEmpty()) {
            throw new ValueConversionException(String.format("unable to determine nodeId of datatype of OPC UA value (datatype: %s)", value.getDataType().get()));
        }
        OpcUaToAasValueConverter converter = opcUaToAasConverters.getOrDefault(
                new ConversionTypeInfo(targetType, valueDatatype.get()),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }


    /**
     * Converts OPC UA value to AAS target type.
     *
     * @param value OPC UAvalue
     * @param targetType AAS target type
     * @param index The desired index in the array
     * @return converted OPC UA value
     * @throws ValueConversionException if value or targetType are null or conversion fails
     */
    public TypedValue<?> convertArray(Variant value, Datatype targetType, String index) throws ValueConversionException {
        if (value == null) {
            throw new ValueConversionException("value must be non-null");
        }
        if (targetType == null) {
            throw new ValueConversionException("targetType value must be non-null");
        }
        if (value.getDataType().isEmpty()) {
            throw new ValueConversionException(String.format("unable to determine datatype of OPC UA value (value: %s)", value));
        }
        if (!value.getValue().getClass().isArray()) {
            return convert(value, targetType);
        }
        if ((index == null) || index.isEmpty()) {
            throw new ValueConversionException("index must not be empty");
        }

        Optional<NodeId> valueDatatype = value.getDataType().get().toNodeId(null);
        if (valueDatatype.isEmpty()) {
            throw new ValueConversionException(String.format("unable to determine nodeId of datatype of OPC UA value (datatype: %s)", value.getDataType().get()));
        }

        value = getArrayElement(value, index);

        OpcUaToAasValueConverter converter = opcUaToAasConverters.getOrDefault(
                new ConversionTypeInfo(targetType, valueDatatype.get()),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }


    /**
     * Sets the given value in the desired element of the given array.
     *
     * @param arrayValue The original array.
     * @param index The desired index.
     * @param indexValue The desired value.
     * @throws ValueConversionException Value cannot be converted.
     */
    public void setArrayElement(Variant arrayValue, String index, Object indexValue) throws ValueConversionException {
        List<Integer> arrayIndizes = getArrayIndices(index);

        Object obj = arrayValue.getValue();
        for (int i = 0; i < arrayIndizes.size() - 1; i++) {
            if (obj.getClass().isArray()) {
                obj = Array.get(obj, arrayIndizes.get(i));
            }
            else {
                throw new ValueConversionException(NOT_ENOUGH_DIMENSION_TXT);
            }
        }

        if (obj.getClass().isArray()) {
            Array.set(obj, arrayIndizes.get(arrayIndizes.size() - 1), indexValue);
        }
        else {
            throw new ValueConversionException(NOT_ENOUGH_DIMENSION_TXT);
        }
    }


    private Variant getArrayElement(Variant value, String index) throws ValueConversionException {
        List<Integer> arrayIndizes = getArrayIndices(index);

        Object obj = value.getValue();
        for (int ind: arrayIndizes) {
            if ((obj.getClass().isArray()) && (Array.getLength(obj) > ind)) {
                obj = Array.get(obj, ind);
            }
            else {
                throw new ValueConversionException(NOT_ENOUGH_DIMENSION_TXT);
            }
        }

        return new Variant(obj);
    }


    private List<Integer> getArrayIndices(String index) throws NumberFormatException {
        List<Integer> arrayIndices = new ArrayList<>();
        Matcher matcher = arrayPattern.matcher(index);
        while (matcher.find()) {
            arrayIndices.add(Integer.valueOf(matcher.group()));
        }
        return arrayIndices;
    }

    private static class DefaultConverter implements AasToOpcUaValueConverter, OpcUaToAasValueConverter {

        @Override
        public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
            if (!BuiltinDataType.isBuiltin(targetType)) {
                throw new ValueConversionException(String.format("encountered unsupported OPC UA data type (node id: %s)", targetType));
            }
            BuiltinDataType builtinDataType = BuiltinDataType.fromNodeId(targetType);
            if (value.getValue() != null && Objects.equals(builtinDataType.getBackingClass(), value.getValue().getClass())) {
                if ((value.getDataType() == Datatype.DATE_TIME) && (targetType == Identifiers.DateTime)) {
                    return new Variant(new DateTime(((ZonedDateTime) value.getValue()).toInstant()));
                }
                return new Variant(value.getValue());
            }
            if ((value.getDataType() == Datatype.DATE_TIME) && (targetType.equals(Identifiers.DateTime))) {
                return new Variant(new DateTime(((ZonedDateTime) value.getValue()).toInstant()));
            }
            return new Variant(ImplicitConversions.convert(value.getValue(), builtinDataType));
        }


        @Override
        public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
            try {
                if ((targetType == Datatype.DATE_TIME) && (value.getValue() instanceof DateTime)) {
                    return TypedValueFactory.create(targetType,
                            ZonedDateTime.ofInstant(((DateTime) value.getValue()).getJavaInstant(), ZoneId.of(DateTimeValue.DEFAULT_TIMEZONE)).toString());
                }
                else {
                    return TypedValueFactory.create(targetType, value.getValue().toString());
                }
            }
            catch (ValueFormatException e) {
                throw new ValueConversionException(String.format("error converting value (value: %s, target datatype: %s",
                        value.getValue().toString(),
                        targetType),
                        e);
            }
        }

    }
}
