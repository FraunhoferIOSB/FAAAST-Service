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
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3006")
public enum AASEntityTypeDataType implements Enumeration {
    CoManagedEntity(0),

    SelfManagedEntity(1);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASEntityTypeDataType> NONE = EnumSet.noneOf(AASEntityTypeDataType.class);

    public static final EnumSet<AASEntityTypeDataType> ALL = EnumSet.allOf(AASEntityTypeDataType.class);

    private static final Map<Integer, AASEntityTypeDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASEntityTypeDataType");
        b.setJavaClass(AASEntityTypeDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3006")));
        b.addMapping(0, "CoManagedEntity");
        b.addMapping(1, "SelfManagedEntity");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASEntityTypeDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASEntityTypeDataType>();
        for (AASEntityTypeDataType i: AASEntityTypeDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASEntityTypeDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASEntityTypeDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASEntityTypeDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASEntityTypeDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASEntityTypeDataType[] valueOf(int[] value) {
        AASEntityTypeDataType[] result = new AASEntityTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASEntityTypeDataType[] valueOf(Integer[] value) {
        AASEntityTypeDataType[] result = new AASEntityTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASEntityTypeDataType[] valueOf(UnsignedInteger[] value) {
        AASEntityTypeDataType[] result = new AASEntityTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASEntityTypeDataType... list) {
        int result = 0;
        for (AASEntityTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASEntityTypeDataType> list) {
        int result = 0;
        for (AASEntityTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASEntityTypeDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASEntityTypeDataType> getSet(int mask) {
        List<AASEntityTypeDataType> res = new ArrayList<AASEntityTypeDataType>();
        for (AASEntityTypeDataType l: AASEntityTypeDataType.values()) {
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
        private AASEntityTypeDataType value;

        private Builder() {}


        @Override
        public AASEntityTypeDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASEntityTypeDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASEntityTypeDataType int value: " + value);
            }
            return this;
        }
    }
}
