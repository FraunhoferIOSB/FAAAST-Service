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

import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.UaNodeId;
import com.prosysopc.ua.stack.builtintypes.Enumeration;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.typedictionary.EnumerationSpecification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3004")
public enum AASValueTypeDataType implements Enumeration {
    Boolean(0),

    SByte(1),

    Byte(2),

    Int16(3),

    UInt16(4),

    Int32(5),

    UInt32(6),

    Int64(7),

    UInt64(8),

    Float(9),

    Double(10),

    String(11),

    DateTime(12),

    ByteString(13),

    LocalizedText(14),

    UtcTime(15);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASValueTypeDataType> NONE = EnumSet.noneOf(AASValueTypeDataType.class);

    public static final EnumSet<AASValueTypeDataType> ALL = EnumSet.allOf(AASValueTypeDataType.class);

    private static final Map<Integer, AASValueTypeDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASValueTypeDataType");
        b.setJavaClass(AASValueTypeDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3004")));
        b.addMapping(0, "Boolean");
        b.addMapping(1, "SByte");
        b.addMapping(2, "Byte");
        b.addMapping(3, "Int16");
        b.addMapping(4, "UInt16");
        b.addMapping(5, "Int32");
        b.addMapping(6, "UInt32");
        b.addMapping(7, "Int64");
        b.addMapping(8, "UInt64");
        b.addMapping(9, "Float");
        b.addMapping(10, "Double");
        b.addMapping(11, "String");
        b.addMapping(12, "DateTime");
        b.addMapping(13, "ByteString");
        b.addMapping(14, "LocalizedText");
        b.addMapping(15, "UtcTime");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASValueTypeDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASValueTypeDataType>();
        for (AASValueTypeDataType i: AASValueTypeDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASValueTypeDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASValueTypeDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASValueTypeDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASValueTypeDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASValueTypeDataType[] valueOf(int[] value) {
        AASValueTypeDataType[] result = new AASValueTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASValueTypeDataType[] valueOf(Integer[] value) {
        AASValueTypeDataType[] result = new AASValueTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASValueTypeDataType[] valueOf(UnsignedInteger[] value) {
        AASValueTypeDataType[] result = new AASValueTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASValueTypeDataType... list) {
        int result = 0;
        for (AASValueTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASValueTypeDataType> list) {
        int result = 0;
        for (AASValueTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASValueTypeDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASValueTypeDataType> getSet(int mask) {
        List<AASValueTypeDataType> res = new ArrayList<AASValueTypeDataType>();
        for (AASValueTypeDataType l: AASValueTypeDataType.values()) {
            if ((mask & l.value) == l.value) {
                res.add(l);
            }
        }
        return EnumSet.copyOf(res);
    }


    @Override
    public int getValue() {
        return value;
    }


    public static Builder builder() {
        return new Builder();
    }


    @Override
    public Builder toBuilder() {
        Builder b = builder();
        b.setValue(this.getValue());
        return b;
    }

    public static class Builder implements Enumeration.Builder {
        private AASValueTypeDataType value;

        private Builder() {}


        @Override
        public AASValueTypeDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASValueTypeDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASValueTypeDataType int value: " + value);
            }
            return this;
        }
    }
}
