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
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3008")
public enum AASDataTypeIEC61360DataType implements Enumeration {
    BOOLEAN(0),

    DATE(1),

    RATIONAL(2),

    RATIONAL_MEASURE(3),

    REAL_COUNT(4),

    REAL_CURRENCY(5),

    REAL_MEASURE(6),

    STRING(7),

    STRING_TRANSLATABLE(8),

    TIME(9),

    TIMESTAMP(10),

    URL(11),

    INTEGER_COUNT(12),

    INTEGER_CURRENCY(13),

    INTEGER_MEASURE(14);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASDataTypeIEC61360DataType> NONE = EnumSet.noneOf(AASDataTypeIEC61360DataType.class);

    public static final EnumSet<AASDataTypeIEC61360DataType> ALL = EnumSet.allOf(AASDataTypeIEC61360DataType.class);

    private static final Map<Integer, AASDataTypeIEC61360DataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASDataTypeIEC61360DataType");
        b.setJavaClass(AASDataTypeIEC61360DataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3008")));
        b.addMapping(0, "BOOLEAN");
        b.addMapping(1, "DATE");
        b.addMapping(2, "RATIONAL");
        b.addMapping(3, "RATIONAL_MEASURE");
        b.addMapping(4, "REAL_COUNT");
        b.addMapping(5, "REAL_CURRENCY");
        b.addMapping(6, "REAL_MEASURE");
        b.addMapping(7, "STRING");
        b.addMapping(8, "STRING_TRANSLATABLE");
        b.addMapping(9, "TIME");
        b.addMapping(10, "TIMESTAMP");
        b.addMapping(11, "URL");
        b.addMapping(12, "INTEGER_COUNT");
        b.addMapping(13, "INTEGER_CURRENCY");
        b.addMapping(14, "INTEGER_MEASURE");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASDataTypeIEC61360DataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASDataTypeIEC61360DataType>();
        for (AASDataTypeIEC61360DataType i: AASDataTypeIEC61360DataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASDataTypeIEC61360DataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASDataTypeIEC61360DataType valueOf(int value) {
        return map.get(value);
    }


    public static AASDataTypeIEC61360DataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASDataTypeIEC61360DataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASDataTypeIEC61360DataType[] valueOf(int[] value) {
        AASDataTypeIEC61360DataType[] result = new AASDataTypeIEC61360DataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASDataTypeIEC61360DataType[] valueOf(Integer[] value) {
        AASDataTypeIEC61360DataType[] result = new AASDataTypeIEC61360DataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASDataTypeIEC61360DataType[] valueOf(UnsignedInteger[] value) {
        AASDataTypeIEC61360DataType[] result = new AASDataTypeIEC61360DataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASDataTypeIEC61360DataType... list) {
        int result = 0;
        for (AASDataTypeIEC61360DataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASDataTypeIEC61360DataType> list) {
        int result = 0;
        for (AASDataTypeIEC61360DataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASDataTypeIEC61360DataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASDataTypeIEC61360DataType> getSet(int mask) {
        List<AASDataTypeIEC61360DataType> res = new ArrayList<AASDataTypeIEC61360DataType>();
        for (AASDataTypeIEC61360DataType l: AASDataTypeIEC61360DataType.values()) {
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
        private AASDataTypeIEC61360DataType value;

        private Builder() {}


        @Override
        public AASDataTypeIEC61360DataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASDataTypeIEC61360DataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASDataTypeIEC61360DataType int value: " + value);
            }
            return this;
        }
    }
}
