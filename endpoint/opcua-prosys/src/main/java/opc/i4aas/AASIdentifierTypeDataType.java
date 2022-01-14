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
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3010")
public enum AASIdentifierTypeDataType implements Enumeration {
    IRDI(0),

    IRI(1),

    Custom(2);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASIdentifierTypeDataType> NONE = EnumSet.noneOf(AASIdentifierTypeDataType.class);

    public static final EnumSet<AASIdentifierTypeDataType> ALL = EnumSet.allOf(AASIdentifierTypeDataType.class);

    private static final Map<Integer, AASIdentifierTypeDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASIdentifierTypeDataType");
        b.setJavaClass(AASIdentifierTypeDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3010")));
        b.addMapping(0, "IRDI");
        b.addMapping(1, "IRI");
        b.addMapping(2, "Custom");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASIdentifierTypeDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASIdentifierTypeDataType>();
        for (AASIdentifierTypeDataType i: AASIdentifierTypeDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASIdentifierTypeDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASIdentifierTypeDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASIdentifierTypeDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASIdentifierTypeDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASIdentifierTypeDataType[] valueOf(int[] value) {
        AASIdentifierTypeDataType[] result = new AASIdentifierTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASIdentifierTypeDataType[] valueOf(Integer[] value) {
        AASIdentifierTypeDataType[] result = new AASIdentifierTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASIdentifierTypeDataType[] valueOf(UnsignedInteger[] value) {
        AASIdentifierTypeDataType[] result = new AASIdentifierTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASIdentifierTypeDataType... list) {
        int result = 0;
        for (AASIdentifierTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASIdentifierTypeDataType> list) {
        int result = 0;
        for (AASIdentifierTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASIdentifierTypeDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASIdentifierTypeDataType> getSet(int mask) {
        List<AASIdentifierTypeDataType> res = new ArrayList<AASIdentifierTypeDataType>();
        for (AASIdentifierTypeDataType l: AASIdentifierTypeDataType.values()) {
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
        private AASIdentifierTypeDataType value;

        private Builder() {}


        @Override
        public AASIdentifierTypeDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASIdentifierTypeDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASIdentifierTypeDataType int value: " + value);
            }
            return this;
        }
    }
}
