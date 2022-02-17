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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.ValueFormatException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.milo.opcua.sdk.server.events.conversions.ImplicitConversions;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;


public class ValueConverter {

    private Map<ConversionInfo, AasToOpcUaValueConverter> aasToOpcUaConverters;
    private Map<ConversionInfo, OpcUaToAasValueConverter> opcUaToAasConverters;

    public ValueConverter() {
        this.aasToOpcUaConverters = new HashMap<>();
        this.opcUaToAasConverters = new HashMap<>();
        register(Datatype.Integer, Identifiers.Integer, new AasToOpcUaValueConverter() {
            @Override
            public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
                //                return new Variant(ImplicitConversions.convert(((BigInteger) value.getValue()).intValueExact(), BuiltinDataType.Int32));
                return new Variant(((BigInteger) value.getValue()).intValueExact());
            }
        });
    }


    public void register(Datatype aasDatatype, NodeId opcUaDatatype, AasToOpcUaValueConverter conveter) {
        aasToOpcUaConverters.put(new ConversionInfo(aasDatatype, opcUaDatatype), conveter);
    }


    public void register(Datatype aasDatatype, NodeId opcUaDatatype, OpcUaToAasValueConverter conveter) {
        opcUaToAasConverters.put(new ConversionInfo(aasDatatype, opcUaDatatype), conveter);
    }


    public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
        if (value == null) {
            throw new ValueConversionException("typed value must be non-null");
        }
        if (targetType == null) {
            throw new ValueConversionException("type value must be non-null");
        }
        AasToOpcUaValueConverter converter = aasToOpcUaConverters.getOrDefault(
                new ConversionInfo(value.getDataType(), targetType),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }


    public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
        if (value == null) {
            throw new ValueConversionException("value must be non-null");
        }
        if (targetType == null) {
            throw new ValueConversionException("datatype value must be non-null");
        }
        OpcUaToAasValueConverter converter = opcUaToAasConverters.getOrDefault(
                new ConversionInfo(targetType, value.getDataType().get().toNodeId(null).get()),
                new DefaultConverter());
        return converter.convert(value, targetType);
    }

    private static class DefaultConverter implements AasToOpcUaValueConverter, OpcUaToAasValueConverter {

        @Override
        public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException {
            if (!BuiltinDataType.isBuiltin(targetType)) {
                throw new ValueConversionException(String.format("encountered unsupported OPC UA data type (node id: %s)", targetType));
            }
            BuiltinDataType builtinDataType = BuiltinDataType.fromNodeId(targetType);
            if (value.getValue() != null && Objects.equals(builtinDataType.getBackingClass(), value.getValue().getClass())) {
                return new Variant(value.getValue());
            }
            return new Variant(ImplicitConversions.convert(value.getValue(), builtinDataType));
        }


        @Override
        public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException {
            try {
                return TypedValueFactory.create(targetType, value.getValue().toString());
            }
            catch (ValueFormatException ex) {
                throw new ValueConversionException(String.format("error converting value (value: %s, target datatype: %s",
                        value.getValue().toString(),
                        targetType),
                        ex);
            }
        }

    }

    private static class ConversionInfo {

        private Datatype aasDatatype;
        private NodeId opcUaDatatype;

        private ConversionInfo(Datatype aasDatatype, NodeId opcUaDatatype) {
            this.aasDatatype = aasDatatype;
            this.opcUaDatatype = opcUaDatatype;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConversionInfo that = (ConversionInfo) o;
            return Objects.equals(aasDatatype, that.aasDatatype)
                    && Objects.equals(opcUaDatatype, that.opcUaDatatype);
        }


        @Override
        public int hashCode() {
            return Objects.hash(aasDatatype, opcUaDatatype);
        }
    }

    public static interface AasToOpcUaValueConverter {

        public Variant convert(TypedValue<?> value, NodeId targetType) throws ValueConversionException;
    }

    public static interface OpcUaToAasValueConverter {

        public TypedValue<?> convert(Variant value, Datatype targetType) throws ValueConversionException;
    }
}
