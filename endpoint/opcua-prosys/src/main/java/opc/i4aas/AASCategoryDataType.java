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
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3007")
public enum AASCategoryDataType implements Enumeration {
    CONSTANT(0),

    PARAMETER(1),

    VARIABLE(2),

    RELATIONSHIP(3);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASCategoryDataType> NONE = EnumSet.noneOf(AASCategoryDataType.class);

    public static final EnumSet<AASCategoryDataType> ALL = EnumSet.allOf(AASCategoryDataType.class);

    private static final Map<Integer, AASCategoryDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASCategoryDataType");
        b.setJavaClass(AASCategoryDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3007")));
        b.addMapping(0, "CONSTANT");
        b.addMapping(1, "PARAMETER");
        b.addMapping(2, "VARIABLE");
        b.addMapping(3, "RELATIONSHIP");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASCategoryDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASCategoryDataType>();
        for (AASCategoryDataType i: AASCategoryDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASCategoryDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASCategoryDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASCategoryDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASCategoryDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASCategoryDataType[] valueOf(int[] value) {
        AASCategoryDataType[] result = new AASCategoryDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASCategoryDataType[] valueOf(Integer[] value) {
        AASCategoryDataType[] result = new AASCategoryDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASCategoryDataType[] valueOf(UnsignedInteger[] value) {
        AASCategoryDataType[] result = new AASCategoryDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASCategoryDataType... list) {
        int result = 0;
        for (AASCategoryDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASCategoryDataType> list) {
        int result = 0;
        for (AASCategoryDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASCategoryDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASCategoryDataType> getSet(int mask) {
        List<AASCategoryDataType> res = new ArrayList<AASCategoryDataType>();
        for (AASCategoryDataType l: AASCategoryDataType.values()) {
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
        private AASCategoryDataType value;

        private Builder() {}


        @Override
        public AASCategoryDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASCategoryDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASCategoryDataType int value: " + value);
            }
            return this;
        }
    }
}
