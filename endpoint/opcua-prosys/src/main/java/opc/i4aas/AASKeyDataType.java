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
package opc.i4aas;

import com.prosysopc.ua.StructureSerializer;
import com.prosysopc.ua.StructureUtils;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.UaNodeId;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.Structure;
import com.prosysopc.ua.stack.encoding.EncoderContext;
import com.prosysopc.ua.stack.utils.AbstractStructure;
import com.prosysopc.ua.typedictionary.FieldSpecification;
import com.prosysopc.ua.typedictionary.StructureSpecification;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3011")
public class AASKeyDataType extends AbstractStructure {
    @Deprecated
    public static final ExpandedNodeId BINARY = Ids.AASKeyDataType_DefaultBinary;

    @Deprecated
    public static final ExpandedNodeId XML = Ids.AASKeyDataType_DefaultXml;

    @Deprecated
    public static final ExpandedNodeId JSON = Ids.AASKeyDataType_DefaultJson;

    @Deprecated
    public static final ExpandedNodeId ID = Ids.AASKeyDataType;

    public static final StructureSpecification SPECIFICATION;

    static {
        StructureSpecification.Builder b = StructureSpecification.builder();
        b.addField(Fields.Type.getSpecification());
        b.addField(Fields.Value.getSpecification());
        b.addField(Fields.IdType.getSpecification());
        b.setBinaryEncodeId(UaNodeId.fromLocal(BINARY));
        b.setXmlEncodeId(UaNodeId.fromLocal(XML));
        b.setJsonEncodeId(UaNodeId.fromLocal(JSON));
        b.setTypeId(UaNodeId.fromLocal(ID));
        b.setName("AASKeyDataType");
        b.setJavaClass(AASKeyDataType.class);
        b.setStructureType(StructureSpecification.StructureType.NORMAL);
        b.setSerializerSupplier(new StructureSpecification.StructureSerializerSupplier() {
            @Override
            public StructureSerializer get(StructureSpecification specification, EncoderContext ctx) {
                return new Serializers.AASKeyDataTypeSerializer();
            }
        });
        b.setBuilderSupplier(new StructureSpecification.StructureBuilderSupplier() {
            @Override
            public Structure.Builder get() {
                return AASKeyDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }

    private AASKeyElementsDataType type;

    private String value;

    private AASKeyTypeDataType idType;

    public AASKeyDataType() {}


    public AASKeyDataType(AASKeyElementsDataType type, String value, AASKeyTypeDataType idType) {
        this.type = type;
        this.value = value;
        this.idType = idType;
    }


    public AASKeyElementsDataType getType() {
        return this.type;
    }


    public void setType(AASKeyElementsDataType type) {
        this.type = type;
    }


    public String getValue() {
        return this.value;
    }


    public void setValue(String value) {
        this.value = value;
    }


    public AASKeyTypeDataType getIdType() {
        return this.idType;
    }


    public void setIdType(AASKeyTypeDataType idType) {
        this.idType = idType;
    }


    @Override
    public AASKeyDataType clone() {
        AASKeyDataType result = (AASKeyDataType) super.clone();
        result.type = StructureUtils.clone(this.type);
        result.value = StructureUtils.clone(this.value);
        result.idType = StructureUtils.clone(this.idType);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AASKeyDataType other = (AASKeyDataType) obj;
        if (!StructureUtils.scalarOrArrayEquals(getType(), other.getType())) {
            return false;
        }
        if (!StructureUtils.scalarOrArrayEquals(getValue(), other.getValue())) {
            return false;
        }
        if (!StructureUtils.scalarOrArrayEquals(getIdType(), other.getIdType())) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        return StructureUtils.hashCode(this.getType(), this.getValue(), this.getIdType());
    }


    @Override
    @Deprecated
    public ExpandedNodeId getBinaryEncodeId() {
        return BINARY;
    }


    @Override
    @Deprecated
    public ExpandedNodeId getXmlEncodeId() {
        return XML;
    }


    @Override
    @Deprecated
    public ExpandedNodeId getJsonEncodeId() {
        return JSON;
    }


    @Override
    @Deprecated
    public ExpandedNodeId getTypeId() {
        return ID;
    }


    @Override
    public StructureSpecification specification() {
        return SPECIFICATION;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Object get(FieldSpecification field) {
        if (Fields.Type.getSpecification().equals(field)) {
            return getType();
        }
        if (Fields.Value.getSpecification().equals(field)) {
            return getValue();
        }
        if (Fields.IdType.getSpecification().equals(field)) {
            return getIdType();
        }
        throw new IllegalArgumentException("Unknown field: " + field);
    }


    @Override
    public void set(FieldSpecification field, Object value) {
        if (Fields.Type.getSpecification().equals(field)) {
            setType((AASKeyElementsDataType) value);
            return;
        }
        if (Fields.Value.getSpecification().equals(field)) {
            setValue((String) value);
            return;
        }
        if (Fields.IdType.getSpecification().equals(field)) {
            setIdType((AASKeyTypeDataType) value);
            return;
        }
        throw new IllegalArgumentException("Unknown field: " + field);
    }


    @Override
    public Builder toBuilder() {
        Builder b = AASKeyDataType.builder();
        b.setType(getType());
        b.setValue(getValue());
        b.setIdType(getIdType());
        return b;
    }

    public enum Fields {
        Type("Type", AASKeyElementsDataType.class, false, UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3012")), -1),

        Value("Value", String.class, false, UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/;i=12")), -1),

        IdType("IdType", AASKeyTypeDataType.class, false, UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3002")), -1);

        private final FieldSpecification value;

        Fields(String name, Class<?> javaClass, boolean isOptional, UaNodeId dataTypeId,
                int valueRank) {
            FieldSpecification.Builder b = FieldSpecification.builder();
            b.setName(name);
            b.setJavaClass(javaClass);
            b.setIsOptional(isOptional);
            b.setDataTypeId(dataTypeId);
            b.setValueRank(valueRank);
            this.value = b.build();
        }


        public FieldSpecification getSpecification() {
            return value;
        }
    }

    public static class Builder extends AbstractStructure.Builder {
        private AASKeyElementsDataType type;

        private String value;

        private AASKeyTypeDataType idType;

        protected Builder() {}


        public Builder setType(AASKeyElementsDataType type) {
            this.type = type;
            return this;
        }


        public Builder setValue(String value) {
            this.value = value;
            return this;
        }


        public Builder setIdType(AASKeyTypeDataType idType) {
            this.idType = idType;
            return this;
        }


        @Override
        public Builder set(FieldSpecification field, Object value) {
            if (Fields.Type.getSpecification().equals(field)) {
                setType((AASKeyElementsDataType) value);
                return this;
            }
            if (Fields.Value.getSpecification().equals(field)) {
                setValue((String) value);
                return this;
            }
            if (Fields.IdType.getSpecification().equals(field)) {
                setIdType((AASKeyTypeDataType) value);
                return this;
            }
            throw new IllegalArgumentException("Unknown field: " + field);
        }


        @Override
        public StructureSpecification specification() {
            return AASKeyDataType.SPECIFICATION;
        }


        @Override
        public AASKeyDataType build() {
            return new AASKeyDataType(type, value, idType);
        }
    }
}
