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
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3015")
public enum AASModelingKindDataType implements Enumeration {
    Template(0),

    Instance(1);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASModelingKindDataType> NONE = EnumSet.noneOf(AASModelingKindDataType.class);

    public static final EnumSet<AASModelingKindDataType> ALL = EnumSet.allOf(AASModelingKindDataType.class);

    private static final Map<Integer, AASModelingKindDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASModelingKindDataType");
        b.setJavaClass(AASModelingKindDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3015")));
        b.addMapping(0, "Template");
        b.addMapping(1, "Instance");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASModelingKindDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASModelingKindDataType>();
        for (AASModelingKindDataType i: AASModelingKindDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASModelingKindDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASModelingKindDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASModelingKindDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASModelingKindDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASModelingKindDataType[] valueOf(int[] value) {
        AASModelingKindDataType[] result = new AASModelingKindDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASModelingKindDataType[] valueOf(Integer[] value) {
        AASModelingKindDataType[] result = new AASModelingKindDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASModelingKindDataType[] valueOf(UnsignedInteger[] value) {
        AASModelingKindDataType[] result = new AASModelingKindDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASModelingKindDataType... list) {
        int result = 0;
        for (AASModelingKindDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASModelingKindDataType> list) {
        int result = 0;
        for (AASModelingKindDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASModelingKindDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASModelingKindDataType> getSet(int mask) {
        List<AASModelingKindDataType> res = new ArrayList<AASModelingKindDataType>();
        for (AASModelingKindDataType l: AASModelingKindDataType.values()) {
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
        private AASModelingKindDataType value;

        private Builder() {}


        @Override
        public AASModelingKindDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASModelingKindDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASModelingKindDataType int value: " + value);
            }
            return this;
        }
    }
}
