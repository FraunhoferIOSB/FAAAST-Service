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
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3002")
public enum AASKeyTypeDataType implements Enumeration {
    IdShort(0),

    FragmentId(1),

    Custom(2),

    IRDI(3),

    IRI(4);

    public static final EnumerationSpecification SPECIFICATION;

    public static final EnumSet<AASKeyTypeDataType> NONE = EnumSet.noneOf(AASKeyTypeDataType.class);

    public static final EnumSet<AASKeyTypeDataType> ALL = EnumSet.allOf(AASKeyTypeDataType.class);

    private static final Map<Integer, AASKeyTypeDataType> map;

    static {
        EnumerationSpecification.Builder b = EnumerationSpecification.builder();
        b.setName("AASKeyTypeDataType");
        b.setJavaClass(AASKeyTypeDataType.class);
        b.setTypeId(UaNodeId.fromLocal(ExpandedNodeId.parseExpandedNodeId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3002")));
        b.addMapping(0, "IdShort");
        b.addMapping(1, "FragmentId");
        b.addMapping(2, "Custom");
        b.addMapping(3, "IRDI");
        b.addMapping(4, "IRI");
        b.setBuilderSupplier(new EnumerationSpecification.EnumerationBuilderSupplier() {
            @Override
            public Enumeration.Builder get() {
                return AASKeyTypeDataType.builder();
            }
        });
        SPECIFICATION = b.build();
    }
    static {
        map = new HashMap<Integer, AASKeyTypeDataType>();
        for (AASKeyTypeDataType i: AASKeyTypeDataType.values()) {
            map.put(i.value, i);
        }
    }

    private final int value;

    AASKeyTypeDataType(int value) {
        this.value = value;
    }


    @Override
    public EnumerationSpecification specification() {
        return SPECIFICATION;
    }


    public static AASKeyTypeDataType valueOf(int value) {
        return map.get(value);
    }


    public static AASKeyTypeDataType valueOf(Integer value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASKeyTypeDataType valueOf(UnsignedInteger value) {
        return value == null ? null : valueOf(value.intValue());
    }


    public static AASKeyTypeDataType[] valueOf(int[] value) {
        AASKeyTypeDataType[] result = new AASKeyTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASKeyTypeDataType[] valueOf(Integer[] value) {
        AASKeyTypeDataType[] result = new AASKeyTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static AASKeyTypeDataType[] valueOf(UnsignedInteger[] value) {
        AASKeyTypeDataType[] result = new AASKeyTypeDataType[value.length];
        for (int i = 0; i < value.length; i++) {
            result[i] = valueOf(value[i]);
        }
        return result;
    }


    public static UnsignedInteger getMask(AASKeyTypeDataType... list) {
        int result = 0;
        for (AASKeyTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static UnsignedInteger getMask(Collection<AASKeyTypeDataType> list) {
        int result = 0;
        for (AASKeyTypeDataType c: list) {
            result |= c.value;
        }
        return UnsignedInteger.getFromBits(result);
    }


    public static EnumSet<AASKeyTypeDataType> getSet(UnsignedInteger mask) {
        return getSet(mask.intValue());
    }


    public static EnumSet<AASKeyTypeDataType> getSet(int mask) {
        List<AASKeyTypeDataType> res = new ArrayList<AASKeyTypeDataType>();
        for (AASKeyTypeDataType l: AASKeyTypeDataType.values()) {
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
        private AASKeyTypeDataType value;

        private Builder() {}


        @Override
        public AASKeyTypeDataType build() {
            return value;
        }


        @Override
        public Builder setValue(int value) {
            this.value = AASKeyTypeDataType.valueOf(value);
            if (this.value == null) {
                throw new IllegalArgumentException("Unknown enum AASKeyTypeDataType int value: " + value);
            }
            return this;
        }
    }
}
