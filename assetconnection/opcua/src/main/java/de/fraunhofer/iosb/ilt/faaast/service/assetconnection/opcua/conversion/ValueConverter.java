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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DurationValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.netty.buffer.ByteBufUtil;
import java.math.BigInteger;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.milo.opcua.sdk.server.events.conversions.ImplicitConversions;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


/**
 * Converts values bi-directional between OPC UA and AAS types.
 */
public class ValueConverter {

    private Map<ConversionTypeInfo, AasToOpcUaValueConverter> aasToOpcUaConverters;
    private Map<ConversionTypeInfo, OpcUaToAasValueConverter> opcUaToAasConverters;

    public ValueConverter() {
        this.aasToOpcUaConverters = new HashMap<>() {};
        this.opcUaToAasConverters = new HashMap<>();
        register(Datatype.INTEGER, Identifiers.Integer, new AasToOpcUaValueConverter() {
            @Override
            public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
                return new Variant(((BigInteger) value.getValue()).intValueExact());
            }
        });
        register(Datatype.DURATION, Identifiers.Duration, new AasToOpcUaValueConverter() {
            @Override
            public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
                return new Variant(Long.valueOf(((DurationValue) value).getValue().getTimeInMillis(new Date())).doubleValue());
            }
        });
        register(Datatype.DURATION, Identifiers.Double, new OpcUaToAasValueConverter() {
            @Override
            public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
                if (Objects.isNull(value) || value.isNull()) {
                    return null;
                }
                try {
                    return new DurationValue(
                            DatatypeFactory.newDefaultInstance().newDuration(
                                    Double.valueOf(value.getValue().toString()).longValue()));
                }
                catch (NumberFormatException e) {
                    throw new ValueConversionException(e);
                }
            }
        });
        register(Datatype.HEX_BINARY, Identifiers.ByteString, new OpcUaToAasValueConverter() {
            @Override
            public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
                return convertByteString(value, targetType);
            }
        });
        register(Datatype.BASE64_BINARY, Identifiers.ByteString, new OpcUaToAasValueConverter() {
            @Override
            public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
                return convertByteString(value, targetType);
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
        Ensure.requireNonNull(value, new ValueConversionException("value must be non-null"));
        Ensure.requireNonNull(targetType, new ValueConversionException("targetType value must be non-null"));
        Ensure.require(value.getDataType().isPresent(), new ValueConversionException(String.format("unable to determine datatype of OPC UA value (value: %s)", value)));

        Optional<NodeId> valueDatatype = value.getDataType().get().toNodeId(null);
        Ensure.require(valueDatatype.isPresent(),
                new ValueConversionException(String.format("unable to determine nodeId of datatype of OPC UA value (datatype: %s)", value.getDataType().get())));
        OpcUaToAasValueConverter converter = opcUaToAasConverters.getOrDefault(
                new ConversionTypeInfo(targetType, valueDatatype.get()),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }


    private TypedValue<?> convertByteString(Variant value, Datatype targetType) throws ValueConversionException {
        if (Objects.isNull(value) || value.isNull()) {
            return null;
        }
        try {
            String txt;
            if (value.getValue() instanceof ByteString bs) {
                txt = ByteBufUtil.hexDump(bs.bytesOrEmpty());
            }
            else {
                txt = value.getValue().toString();
            }
            return TypedValueFactory.create(targetType, txt);
        }
        catch (NumberFormatException | ValueFormatException e) {
            throw new ValueConversionException(e);
        }
    }

    private static class DefaultConverter implements AasToOpcUaValueConverter, OpcUaToAasValueConverter {

        @Override
        public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
            if (Objects.isNull(value) || Objects.isNull(value.getValue())) {
                return new Variant(null);
            }
            if (!BuiltinDataType.isBuiltin(targetType)) {
                throw new ValueConversionException(String.format("encountered unsupported OPC UA data type (node id: %s)", targetType));
            }
            BuiltinDataType builtinDataType = BuiltinDataType.fromNodeId(targetType);
            if (value.getValue() != null && Objects.equals(builtinDataType.getBackingClass(), value.getValue().getClass())) {
                if ((value.getDataType() == Datatype.DATE_TIME) && (targetType == Identifiers.DateTime)) {
                    return new Variant(new DateTime(((OffsetDateTime) value.getValue()).toInstant()));
                }
                return new Variant(value.getValue());
            }
            if ((value.getDataType() == Datatype.DATE_TIME) && (targetType.equals(Identifiers.DateTime))) {
                return new Variant(new DateTime(((OffsetDateTime) value.getValue()).toInstant()));
            }
            Object implicit = ImplicitConversions.convert(value.getValue(), builtinDataType);
            if (Objects.isNull(implicit) && Objects.equals(String.class, builtinDataType.getBackingClass())) {
                return new Variant(value.asString());
            }
            return new Variant(implicit);
        }


        @Override
        public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
            try {
                if ((targetType == Datatype.DATE_TIME) && (value.getValue() instanceof DateTime)) {
                    return TypedValueFactory.create(targetType,
                            OffsetDateTime.ofInstant(((DateTime) value.getValue()).getJavaInstant(), ZoneId.systemDefault()).toString());
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
